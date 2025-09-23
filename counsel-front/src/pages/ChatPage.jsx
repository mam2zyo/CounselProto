// src/pages/ChatPage.jsx
import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import ChatMessage from "../components/ChatMessage";
import ChatInput from "../components/ChatInput";
import { streamChat } from "../api/chat";
import {
  updateConversation,
  deleteConversation,
  fetchConversations,
  fetchConversationDetail,
} from "../api/conversation";
// ✨ [추가] 결제 관련 API 임포트
import { checkAccessStatus, updateAccessStatus } from "../api/payment";

function ChatPage() {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const mainContentRef = useRef(null);
  const tempMessagesRef = useRef([]);

  // API 기반 상태 관리
  const [conversations, setConversations] = useState([]);
  const [tempMessages, setTempMessages] = useState([]);

  const [activeConversationId, setActiveConversationId] = useState(null);
  const [editingConversationId, setEditingConversationId] = useState(null);

  const [input, setInput] = useState("");
  const [chatMode, setChatMode] = useState("text"); // 'text', 'voiceInput', 'voiceChat'
  const [isSending, setIsSending] = useState(false);

  // ✨ [추가] 이용권 만료일 상태 관리
  const [accessUntil, setAccessUntil] = useState(null);

  // 현재 활성화된 대화 찾기
  const activeConversation = conversations.find(
    (c) => c.id === activeConversationId
  );

  const displayMessages = activeConversationId
    ? activeConversation?.messages
    : tempMessages;

  // ✨ [수정] 로그인 상태에 따라 대화 및 이용권 상태를 로드
  useEffect(() => {
    if (isLoggedIn) {
      loadConversations();
      checkUserAccess(); // 로그인 시 이용권 만료일 확인
    } else {
      navigate("/login");
    }
  }, [isLoggedIn, navigate]);

  useEffect(() => {
    tempMessagesRef.current = tempMessages;
  }, [tempMessages]);

  useEffect(() => {
    if (mainContentRef.current) {
      mainContentRef.current.scrollTop = mainContentRef.current.scrollHeight;
    }
  }, [displayMessages]);

  const loadConversations = async () => {
    try {
      const response = await fetchConversations();
      setConversations(response.data);
    } catch (error) {
      console.error("대화 목록 로딩 실패:", error);
    }
  };

  // ✨ [추가] 이용권 만료일 확인 함수
  const checkUserAccess = async () => {
    try {
      const response = await checkAccessStatus();
      setAccessUntil(response.data.accessUntil);
    } catch (error) {
      console.error("이용권 상태 로딩 실패:", error);
    }
  };

  // ✨ [추가] 포트원 결제창 호출 함수
  const requestPay = () => {
    const { IMP } = window;
    IMP.init('imp04144282'); // 포트원 고객사 식별코드

    IMP.request_pay({
      pg: "html5_inicis",
      pay_method: "card",
      merchant_uid: `mid_${Date.now()}`,
      name: "한 달 이용권",
      amount: 10000,
      buyer_email: "test@example.com",
      buyer_name: "홍길동",
      buyer_tel: "010-1234-5678",
    }, async (rsp) => {
      if (rsp.success) {
        try {
          // 결제 성공 시, 백엔드에 결제 정보 검증 및 만료일 갱신 요청
          await updateAccessStatus({
            imp_uid: rsp.imp_uid,
            merchant_uid: rsp.merchant_uid,
          });
          alert('결제가 완료되었습니다. 이제 한 달 동안 무제한으로 이용할 수 있습니다.');
          checkUserAccess(); // 갱신된 만료일 다시 가져와서 상태 업데이트
        } catch (error) {
          console.error("결제 검증 실패:", error);
          alert("결제는 성공했으나, 이용권 갱신에 실패했습니다. 관리자에게 문의해주세요.");
        }
      } else {
        alert(`결제에 실패하였습니다. 에러 내용: ${rsp.error_msg}`);
      }
    });
  };

  const handleModeSwitch = (mode) => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }
    setChatMode(mode);
    if (mode === "voiceChat") {
      setActiveConversationId(null);
    }
  };

  const handleNewConversation = async () => {
    setActiveConversationId(null);
    setTempMessages([]);
  };

  const handleDeleteConversation = async (id) => {
    if (!confirm("이 대화를 삭제하시겠습니까?")) return;
    try {
      await deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeConversationId === id) {
        setActiveConversationId(null);
      }
      if (editingConversationId === id) {
        setEditingConversationId(null);
      }
    } catch (error) {
      console.error("대화 삭제 실패:", error);
    }
  };

  const handleStartEdit = (id) => {
    setEditingConversationId(id);
  };

  const handleCancelEdit = () => {
    setEditingConversationId(null);
  };

  const handleUpdateConversation = async (id, data) => {
    try {
      const response = await updateConversation(id, data);
      const updatedConversation = response.data;

      setConversations((prev) =>
        prev.map((c) =>
          c.id === id ? { ...c, title: updatedConversation.title } : c
        )
      );
      setEditingConversationId(null);
    } catch (error) {
      console.error("대화 수정 실패:", error);
      alert("대화 수정에 실패했습니다.");
    }
  };

  const handleSelectConversation = async (id) => {
    if (editingConversationId === id) return;
    setActiveConversationId(id);
    setTempMessages([]);

    const selectedConv = conversations.find((c) => c.id === id);
    if (!selectedConv || !selectedConv.messages) {
      try {
        const response = await fetchConversationDetail(id);
        const detail = response.data;
        setConversations((prev) =>
          prev.map((c) => (c.id === id ? { ...c, ...detail } : c))
        );
      } catch (error) {
        console.error("대화 상세 정보 로딩 실패:", error);
      }
    }
  };

  // 스트리밍을 통한 메시지 전송
  const handleSendMessage = async () => {
    if (input.trim() === "" || !isLoggedIn || isSending) return;

    // ✨ [추가/수정] 이용권 만료 여부 확인 후 결제 유도
    const now = new Date();
    const expiry = accessUntil ? new Date(accessUntil) : null;

    if (!expiry || now > expiry) {
      alert("한 달 이용권이 만료되었습니다. 결제 후 다시 이용해주세요.");
      requestPay(); // 결제창 띄우기
      return; // 메시지 전송 중단
    }

    const userMessageText = input.trim();
    setInput("");
    setIsSending(true);

    const isNewConversation = activeConversationId === null;

    const userMessage = {
      id: Date.now(),
      message: userMessageText,
      sender: "user",
    };

    const aiMessageId = Date.now() + 1;
    const aiMessagePlaceholder = {
      id: aiMessageId,
      message: "...",
      sender: "ai",
    };

    if (isNewConversation) {
      setTempMessages((prev) => [...prev, userMessage, aiMessagePlaceholder]);
    } else {
      setConversations((prev) =>
        prev.map((c) =>
          c.id === activeConversationId
            ? {
                ...c,
                messages: [
                  ...(c.messages || []),
                  userMessage,
                  aiMessagePlaceholder,
                ],
              }
            : c
        )
      );
    }

    streamChat(
      activeConversationId,
      userMessageText,
      (token) => {
        const updater = (messages) => {
          const lastMessage = messages[messages.length - 1];
          if (lastMessage && lastMessage.id === aiMessageId) {
            const newText =
              lastMessage.message === "..."
                ? token
                : lastMessage.message + token;
            return [
              ...messages.slice(0, -1),
              { ...lastMessage, message: newText },
            ];
          }
          return messages;
        };

        if (isNewConversation) {
          setTempMessages(updater);
        } else {
          setConversations((prev) =>
            prev.map((c) =>
              c.id === activeConversationId
                ? { ...c, messages: updater(c.messages) }
                : c
            )
          );
        }
      },
      (error) => {
        setIsSending(false);
      },
      async () => {
        setIsSending(false);
        if (!isNewConversation) return;

        try {
          const { data: list } = await fetchConversations();
          const newConv = list[0];
          setConversations((prev) => [
            { ...newConv, messages: tempMessagesRef.current },
            ...prev,
          ]);
          setActiveConversationId(newConv.id);
          setTempMessages([]);
        } catch (e) {
          console.error(e);
        }
      }
    );
  };

  return (
    <div className="drawer lg:drawer-open">
      <input id="my-drawer" type="checkbox" className="drawer-toggle" />
      <div className="drawer-content flex flex-col h-screen">
        <Navbar />

        <main ref={mainContentRef} className="flex-1 overflow-y-auto p-4">
          {/* ✨ [수정] 이용권 만료 시 결제 유도 UI를 보여줌 */}
          {(!accessUntil || new Date() > new Date(accessUntil)) ? (
            <div className="flex flex-col items-center justify-center h-full text-gray-400">
              <p className="text-lg mb-4">한 달 이용권이 필요합니다.</p>
              <button onClick={requestPay} className="btn btn-primary">
                한 달 이용권 구매하기 (₩10,000)
              </button>
            </div>
          ) : (
            // 이용권이 있을 때만 메시지 목록을 렌더링
            displayMessages && displayMessages.length > 0 ? (
              displayMessages.map((message) => (
                <ChatMessage key={message.id} message={message} />
              ))
            ) : (
              <div className="flex items-center justify-center h-screen">
                <p className="text-gray-400 text-center -mt-50">
                  새로운 대화를 시작해보세요 ✨
                </p>
              </div>
            )
          )}
        </main>
        <ChatInput
          input={input}
          setInput={setInput}
          chatMode={chatMode}
          handleSendMessage={handleSendMessage}
          handleModeSwitch={handleModeSwitch}
        />
      </div>
      <Sidebar
        conversations={conversations}
        activeConversationId={activeConversationId}
        onNewConversation={handleNewConversation}
        onSelectConversation={handleSelectConversation}
        onDeleteConversation={handleDeleteConversation}
        onUpdateConversation={handleUpdateConversation}
        editingId={editingConversationId}
        onStartEdit={handleStartEdit}
        onCancelEdit={handleCancelEdit}
      />
    </div>
  );
}

export default ChatPage;
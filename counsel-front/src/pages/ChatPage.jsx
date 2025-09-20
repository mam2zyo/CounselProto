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

  // 현재 활성화된 대화 찾기
  const activeConversation = conversations.find(
    (c) => c.id === activeConversationId
  );

  const displayMessages = activeConversationId
    ? activeConversation?.messages
    : tempMessages;

  useEffect(() => {
    if (isLoggedIn) {
      loadConversations();
    }
  }, [isLoggedIn]);

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

  // 새 대화 생성 핸들러
  const handleNewConversation = async () => {
    setActiveConversationId(null);
    setTempMessages([]);
  };

  // 대화 삭제 핸들러
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

  // 수정 시작 핸들러
  const handleStartEdit = (id) => {
    setEditingConversationId(id);
  };

  // 수정 취소 핸들러
  const handleCancelEdit = () => {
    setEditingConversationId(null);
  };

  // 대화 제목 수정 핸들러
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

  // 사이드바에서 특정 대화 선택시
  const handleSelectConversation = async (id) => {
    if (editingConversationId === id) return; // 수정 중 다른 대화 선택 막기
    setActiveConversationId(id);
    setTempMessages([]);

    // 선택된 대화에 messages가 없으면 로드
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

    const userMessageText = input.trim();
    setInput("");
    setIsSending(true);

    const isNewConversation = activeConversationId === null;

    // 사용자 메시지를 ui 에 반영
    const userMessage = {
      id: Date.now(),
      message: userMessageText,
      sender: "user",
    };

    // AI 응답 메시지 placeholder 추가
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

    // 스트리밍 시작
    streamChat(
      activeConversationId, // null 또는 실제 ID 전달
      userMessageText,
      (token) => {
        // onMessage
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
        /* onError: 에러 처리 (기존과 유사하게 구현) */
        setIsSending(false);
      },

      async () => {
        // onComplete
        setIsSending(false);
        if (!isNewConversation) return;

        try {
          const { data: list } = await fetchConversations();
          const newConv = list[0]; // 서버가 최신순으로 내려준다고 가정
          setConversations((prev) => [
            { ...newConv, messages: tempMessagesRef.current },
            ...prev,
          ]);
          setActiveConversationId(newConv.id);
          setTempMessages([]); // 임시 메시지 초기화
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
          {/* ✨ [수정] displayMessages 사용 */}
          {!displayMessages || displayMessages.length === 0 ? (
            <div className="flex items-center justify-center h-screen">
              <p className="text-gray-400 text-center -mt-50">
                새로운 대화를 시작해보세요 ✨
              </p>
            </div>
          ) : (
            // 메시지가 하나라도 있는 경우, 메시지 목록을 렌더링
            displayMessages.map((message) => (
              <ChatMessage key={message.id} message={message} />
            ))
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

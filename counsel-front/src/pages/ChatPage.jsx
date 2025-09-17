// src/pages/ChatPage.jsx
import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import ChatMessage from "../components/ChatMessage";
import ChatInput from "../components/ChatInput";
import { VoiceChatIcon } from "../components/Icons";
import {
  fetchConversations,
  createConversation,
  deleteConversation,
  fetchConversationDetail,
  streamChat
} from "../api/conversation";

function ChatPage() {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const mainContentRef = useRef(null);

  // API 기반 상태 관리
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [input, setInput] = useState("");
  const [chatMode, setChatMode] = useState("text"); // 'text', 'voiceInput', 'voiceChat'
  const [isSending, setIsSending] = useState(false);

  // 현재 활성화된 대화 찾기
  const activeConversation = conversations.find(
    (c) => c.id === activeConversationId
  );

  // 컴포넌트 마운트 시 대화목록 불러오기
  useEffect(() => {
    if (isLoggedIn) {
      loadConversations();
    }
  }, [isLoggedIn]);

  const loadConversations = async () => {
    try {
      const response = await fetchConversations();
      setConversations(response.data);
    } catch (error) {
      console.error("대화 목록 로딩 실패:", error);
    }
  };

  // 스크롤 자동 이동, 메시지 추가 시마다 실행
  useEffect(() => {
    if (mainContentRef.current) {
      mainContentRef.current.scrollTop = mainContentRef.current.scrollHeight;
    }
  }, [activeConversation?.messages]);


  // 새 대화 생성 핸들러
  const handleNewConversation = async () => {
    try {
      const response = await createConversation();
      const newConversation = response.data;
      setConversations((prev) => [newConversation, ...prev]);
      setActiveConversationId(newConversation.id);
    } catch (error) {
      console.error("새 대화 생성 실패:", error);
    }
  };


  // 대화 삭제 핸들러
  const handleDeleteConversation = async (id) => {
    if (!confirm("이 대화를 삭제하시겠습니까?")) return;
    try {
      await deleteConversation(id);
      setConversations((prev) => prev.filter(c => c.id !== id));
      if (activeConversationId === id) {
        setActiveConversationId(null);
      }
    } catch (error) {
      console.error("대화 삭제 실패:", error);
    }
  };


  // 사이드바에서 특정 대화 선택시
  const handleSelectConversation = async (id) => {
    setActiveConversationId(id);

    try {
      const response = await fetchConversationDetail(id);
      const detail = response.data;
      setConversations(prev =>
        prev.map(c => c.id === id ? { ...c, messages: detail.messages } : c)
      );
    } catch (error) {
      console.error("대화 상세 정보 로딩 실패:", error);
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

  // 스트리밍을 통한 메시지 전송
  const handleSendMessage = async () => {
    if (input.trim() === "" || !isLoggedIn || isSending) return;

    let currentConvId = activeConversationId;
    const userMessageText = input.trim();
    setInput("");
    setIsSending(true);

    // 활성 대화가 없으면 생성
    if (!currentConvId) {
      try {
        const response = await createConversation();
        const newConversation = response.data;
        setConversations((prev) => [newConversation, ...prev]);
        setActiveConversationId(newConversation.id);
        currentConvId = newConversation.id;
      } catch (error) {
        console.error("메시지 전송 중 새 대화 생성 실패:", error);
        setIsSending(false);
        return;
      }
    }

    // 사용자 메시지를 ui 에 반영
    const userMessage = {
      id: Date.now(),
      message: userMessageText,
      sender: "user"
    };

    setConversations(prev =>
      prev.map(c =>
        c.id === currentConvId
          ? { ...c, messages: [...(c.messages || []), userMessage] }
          : c
      )
    );

    // AI 응답 메시지 placeholder 추가
    const aiMessageId = Date.now() + 1;
    const aiMessagePlaceholder = { id: aiMessageId, message: "...", sender: "ai" };
    setConversations(prev =>
      prev.map(c =>
        c.id === currentConvId ? { ...c, messages: [...c.messages, aiMessagePlaceholder] } : c
      )
    );

    // 스트리밍 시작
    streamChat(
      currentConvId,
      userMessageText,
      (token) => { // onMessage: 스트리밍 데이터 수신 시
        setConversations(prev =>
          prev.map(c => {
            if (c.id === currentConvId) {
              const lastMessage = c.messages[c.messages.length - 1];
              // placeholder를 실제 AI 응답으로 교체하며 텍스트 누적
              if (lastMessage.id === aiMessageId) {
                const newText = lastMessage.message === "..." ? token : lastMessage.message + token;
                const updatedMessages = [...c.messages.slice(0, -1), { ...lastMessage, message: newText }];
                return { ...c, messages: updatedMessages };
              }
            }
            return c;
          })
        );
      },
      (error) => { // onError: 에러 발생 시
        console.error("스트리밍 에러:", error);
        // 에러 메시지를 UI에 표시
        setConversations(prev =>
          prev.map(c => {
            if (c.id === currentConvId) {
              const updatedMessages = [...c.messages.slice(0, -1), { ...aiMessagePlaceholder, message: "오류가 발생했습니다." }];
              return { ...c, messages: updatedMessages };
            }
            return c;
          })
        );
        setIsSending(false);
      },
      () => { // onComplete: 스트림 완료 시
        setIsSending(false);
      }
    );
  };

  return (
    <div className="drawer lg:drawer-open">
      <input id="my-drawer" type="checkbox" className="drawer-toggle" />
      <div className="drawer-content flex flex-col h-screen">
        <Navbar />

        <main ref={mainContentRef} className="flex-1 overflow-y-auto p-4">
          {/* 활성화된 대화가 없거나, 메시지 배열이 비어있는 경우 안내 문구 표시 */}
          {(!activeConversation || !activeConversation.messages || activeConversation.messages.length === 0)
            ? (
              <div className="flex items-center justify-center h-screen">
                <p className="text-gray-400 text-center -mt-50">
                  새로운 대화를 시작해보세요 ✨
                </p>
              </div>
            ) : (
              // 메시지가 하나라도 있는 경우, 메시지 목록을 렌더링
              activeConversation.messages.map((message) => (
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
      />
    </div>
  );
}

export default ChatPage;

// const renderChatContent = () => {
//   if (chatMode === "voiceChat") {
//     return (
//       <div className="flex flex-col items-center justify-center h-full text-base-content/70">
//         <VoiceChatIcon />
//         <p className="mt-4 text-lg">음성 대화 모드가 활성화되었습니다.</p>
//         <p>마이크 버튼을 눌러 대화를 시작하세요.</p>
//       </div>
//     );
//   }

//   if (!activeConversation) {
//     return (
//       <div className="flex items-center justify-center h-screen">
//         <p className="text-gray-400 text-center -mt-50">
//           새로운 대화를 시작해보세요 ✨
//         </p>
//       </div>
//     );
//   }

//   return activeConversation.messages.map((message) => (
//     <ChatMessage key={message.id} message={message} />
//   ));
// };
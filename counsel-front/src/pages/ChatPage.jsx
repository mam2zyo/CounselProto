// src/pages/ChatPage.jsx
import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import ChatMessage from "../components/ChatMessage";
import ChatInput from "../components/ChatInput";
import { VoiceChatIcon } from "../components/Icons";
import { sendMessage as sendMessageApi } from "../api/chat";

function ChatPage() {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();
  const mainContentRef = useRef(null);

  // 🔹 localStorage 기반 상태 초기화
  const [conversations, setConversations] = useState(() => {
    const saved = localStorage.getItem("conversations");
    return saved ? JSON.parse(saved) : [];
  });

  const [activeConversationId, setActiveConversationId] = useState(() => {
    const savedId = localStorage.getItem("activeConversationId");
    return savedId ? Number(savedId) : null;
  });

  const [input, setInput] = useState("");
  const [chatMode, setChatMode] = useState("text"); // 'text', 'voiceInput', 'voiceChat'

  // 🔹 상태 변경 시 localStorage 저장
  useEffect(() => {
    localStorage.setItem("conversations", JSON.stringify(conversations));
  }, [conversations]);

  useEffect(() => {
    if (activeConversationId !== null) {
      localStorage.setItem("activeConversationId", activeConversationId);
    }
  }, [activeConversationId]);

  const activeConversation = conversations.find(
    (c) => c.id === activeConversationId
  );

  // 스크롤 자동 이동
  useEffect(() => {
    if (
      chatMode !== "voiceChat" &&
      mainContentRef.current &&
      activeConversation
    ) {
      mainContentRef.current.scrollTop =
        mainContentRef.current.scrollHeight;
    }
  }, [activeConversation, chatMode]);

  // 메시지 전송
  const handleSendMessage = async () => {
    if (input.trim() === "" || !isLoggedIn) return;

    const userMessageText = input.trim();
    let currentId = activeConversationId;

    if (!currentId) {
      currentId = Date.now();
      setConversations((prev) => [
        ...prev,
        { id: currentId, title: userMessageText.slice(0, 10), messages: [] },
      ]);
      setActiveConversationId(currentId);
    }

    const newUserMessage = {
      id: Date.now(),
      text: userMessageText,
      sender: "user",
    };

    setConversations((prev) =>
      prev.map((c) =>
        c.id === currentId
          ? { ...c, messages: [...c.messages, newUserMessage] }
          : c
      )
    );
    setInput("");

    try {
      const response = await sendMessageApi(userMessageText);
      const newAiMessage = {
        id: Date.now() + 1,
        text: response.data.aiMessage,
        sender: "ai",
      };

      setConversations((prev) =>
        prev.map((c) =>
          c.id === currentId
            ? { ...c, messages: [...c.messages, newAiMessage] }
            : c
        )
      );
    } catch (error) {
      console.error("Chat API Error:", error);
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

  const renderChatContent = () => {
    if (chatMode === "voiceChat") {
      return (
        <div className="flex flex-col items-center justify-center h-full text-base-content/70">
          <VoiceChatIcon />
          <p className="mt-4 text-lg">음성 대화 모드가 활성화되었습니다.</p>
          <p>마이크 버튼을 눌러 대화를 시작하세요.</p>
        </div>
      );
    }

    if (!activeConversation) {
      return (
        <p className="text-gray-400 text-center mt-10">
          새로운 대화를 시작해보세요 ✨
        </p>
      );
    }

    return activeConversation.messages.map((message) => (
      <ChatMessage key={message.id} message={message} />
    ));
  };

  return (
    <div className="drawer lg:drawer-open">
      <input id="my-drawer" type="checkbox" className="drawer-toggle" />
      <div className="drawer-content flex flex-col h-screen">
        <Navbar />
        <main ref={mainContentRef} className="flex-1 overflow-y-auto p-4">
          {renderChatContent()}
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
        setConversations={setConversations}
        activeConversationId={activeConversationId}
        setActiveConversationId={setActiveConversationId}
      />
    </div>
  );
}

export default ChatPage;

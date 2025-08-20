import React from "react";
import { useState, useEffect, useRef } from "react";

// 아이콘 컴포넌트들
const MicIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className="h-6 w-6"
    fill="none"
    viewBox="0 0 24 24"
    stroke="currentColor"
    strokeWidth={2}
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"
    />
  </svg>
);
const VoiceChatIcon = () => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="currentColor"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M7.167 15.416V4.583a.75.75 0 0 1 1.5 0v10.833a.75.75 0 0 1-1.5 0Zm4.166-2.5V7.083a.75.75 0 0 1 1.5 0v5.833a.75.75 0 0 1-1.5 0ZM3 11.25V8.75a.75.75 0 0 1 1.5 0v2.5a.75.75 0 0 1-1.5 0Zm12.5 0V8.75a.75.75 0 0 1 1.5 0v2.5a.75.75 0 0 1-1.5 0Z"></path>
  </svg>
);
const SendIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="currentColor"
    className="w-6 h-6"
  >
    <path d="M3.478 2.404a.75.75 0 0 0-.926.941l2.432 7.905H13.5a.75.75 0 0 1 0 1.5H4.984l-2.432 7.905a.75.75 0 0 0 .926.94 60.519 60.519 0 0 0 18.445-8.986.75.75 0 0 0 0-1.218A60.517 60.517 0 0 0 3.478 2.404Z" />
  </svg>
);

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [chatMode, setChatMode] = useState("text"); // 'text', 'voiceInput', 'voiceChat'
  const [isRecording, setIsRecording] = useState(false);
  const mainContentRef = useRef(null);

  useEffect(() => {
    if (chatMode !== "voiceChat" && mainContentRef.current) {
      mainContentRef.current.scrollTop = mainContentRef.current.scrollHeight;
    }
  }, [messages, chatMode]);

  const handleModeChange = () => {
    const modes = ["text", "voiceInput", "voiceChat"];
    const currentIndex = modes.indexOf(chatMode);
    const nextIndex = (currentIndex + 1) % modes.length;
    setChatMode(modes[nextIndex]);

    if (modes[nextIndex] === "voiceChat") {
      setMessages([]);
    }
  };

  const handleSendMessage = () => {
    if (input.trim() === "") return;

    const newUserMessage = {
      id: Date.now(),
      text: input,
      sender: "user",
    };

    setMessages((prevMessages) => [...prevMessages, newUserMessage]);

    setInput("");

    setTimeout(() => {
      const newAiMessage = {
        id: Date.now() + 1,
        text: `'${input}'에 대한 응답입니다. 지금은 정해진 답변만 할 수 있어요.`,
        sender: "ai",
      };
      setMessages((prevMessages) => [...prevMessages, newAiMessage]);

      if (chatMode === "voiceChat") {
        console.log("TTS 재생 (음성 채팅 모드):", newAiMessage);
      }
    }, 1000);
  };

  const handleMicClick = () => {
    setIsRecording(!isRecording);
    if (!isRecording) {
      console.log("음성 녹음 시작...");
      // 실제 STT 시작 로직
    } else {
      console.log("음성 녹음 중지. 텍스트 변환 및 전송 처리...");
      // 실제 STT 중지 및 결과 처리 로직. 결과 텍스트를 setInput 후 handleSendMessage 호출.
      // 예시: setInput("음성인식으로 변환된 텍스트입니다.");
      //       handleSendMessage();
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

    return messages.map((message) => (
      <div
        key={message.id}
        className={`chat ${
          message.sender === "user" ? "chat-end" : "chat-start"
        }`}
      >
        <div
          className={`chat-bubble ${
            message.sender === "user" ? "chat-bubble-primary" : ""
          }`}
        >
          {message.text}
        </div>
      </div>
    ));
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="drawer lg:drawer-open">
      {/* drawer 상태를 관리하는 숨겨진 체크박스입니다. */}
      <input id="my-drawer" type="checkbox" className="drawer-toggle" />

      {/* 메인 콘텐츠 영역입니다. (채팅 화면) */}
      <div className="drawer-content flex flex-col h-screen">
        {/* 상단 Navbar */}
        <div className="navbar bg-base-200">
          <div className="navbar-start">
            {/* 모바일 화면에서 drawer를 열기 위한 버튼입니다. */}
            <label
              htmlFor="my-drawer"
              className="btn btn-square btn-ghost lg:hidden"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                className="inline-block w-5 h-5 stroke-current"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 6h16M4 12h16M4 18h16"
                ></path>
              </svg>
            </label>
          </div>
          <div className="navbar-center">
            <a className="btn btn-ghost text-xl">마음소리</a>
          </div>

          <div className="navbar-end gap-2">
            <button
              className="btn btn-ghost btn-circle"
              onClick={handleModeChange}
            >
              {chatMode === "text" && <span className="kbd kbd-sm">A</span>}
              {chatMode === "voiceInput" && <MicIcon />}
              {chatMode === "voiceChat" && <VoiceChatIcon />}
            </button>
            <label className="swap swap-rotate">
              <input
                type="checkbox"
                className="theme-controller"
                value="dark"
              />
              <svg
                className="swap-off fill-current w-6 h-6"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
              >
                <path d="M5.64,17l-.71.71a1,1,0,0,0,0,1.41,1,1,0,0,0,1.41,0l.71-.71A1,1,0,0,0,5.64,17ZM5,12a1,1,0,0,0-1-1H3a1,1,0,0,0,0,2H4A1,1,0,0,0,5,12Zm7-7a1,1,0,0,0,1-1V3a1,1,0,0,0-2,0V4A1,1,0,0,0,12,5ZM5.64,7.05a1,1,0,0,0,.7.29,1,1,0,0,0,.71-.29l.71-.71A1,1,0,0,0,6.36,5.64l-.71.71A1,1,0,0,0,5.64,7.05ZM18.36,17A1,1,0,0,0,17,18.36l.71.71a1,1,0,0,0,1.41,0,1,1,0,0,0,0-1.41ZM20,12a1,1,0,0,0-1-1H18a1,1,0,0,0,0,2h1A1,1,0,0,0,20,12ZM17,6.36a1,1,0,0,0,.71-.29,1,1,0,0,0,0-1.41l-.71-.71a1,1,0,0,0-1.41,1.41ZM12,6.5A5.5,5.5,0,1,0,17.5,12,5.51,5.51,0,0,0,12,6.5Zm0,9A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z" />
              </svg>
              <svg
                className="swap-on fill-current w-6 h-6"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
              >
                <path d="M21.64,13a1,1,0,0,0-1.05-.14,8.05,8.05,0,0,1-3.37.73A8.15,8.15,0,0,1,9.08,5.49a8.59,8.59,0,0,1,.25-2A1,1,0,0,0,8,2.36,10.14,10.14,0,1,0,22,14.05,1,1,0,0,0,21.64,13Zm-9.5,6.69A8.14,8.14,0,0,1,7.08,5.22v.27A10.15,10.15,0,0,0,17.22,15.63a9.79,9.79,0,0,0,2.1-.22A8.11,8.11,0,0,1,12.14,19.73Z" />
              </svg>
            </label>
          </div>
        </div>

        <main ref={mainContentRef} className="flex-1 overflow-y-auto p-4">
          {renderChatContent()}
        </main>

        {/* 메시지 입력창 */}
        <div className="p-4 bg-base-200">
          <div className="flex items-center gap-2">
            <textarea
              className="textarea textarea-bordered w-full"
              placeholder="메시지를 입력하세요..."
              value={input}
              onKeyDown={handleKeyDown}
              onChange={(e) => setInput(e.target.value)}
              disabled={chatMode !== "text"}
            />

            {chatMode === "text" && (
              <button className="btn btn-primary" onClick={handleSendMessage}>
                <SendIcon />
              </button>
            )}

            {(chatMode === "voiceInput" || chatMode === "voiceChat") && (
              <button
                className={`btn ${
                  isRecording ? "btn-error" : "btn-primary"
                } btn-circle`}
                onClick={handleMicClick}
              >
                <MicIcon />
              </button>
            )}
          </div>
        </div>
      </div>

      {/* 사이드바 영역입니다. (채팅 기록 목록) */}
      <div className="drawer-side">
        <label
          htmlFor="my-drawer"
          aria-label="close sidebar"
          className="drawer-overlay"
        ></label>
        {/* menu 컴포넌트를 사용해 채팅 기록 목록을 만듭니다. */}
        <ul className="menu p-4 w-80 min-h-full bg-base-100 text-base-content">
          <li className="menu-title">Chat History</li>
          <li>
            <a>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                />
              </svg>{" "}
              Chat 1: daisyUI에 관하여
            </a>
          </li>
          <li className="menu-active">
            <a>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                />
              </svg>{" "}
              Chat 2: Spring Boot 프로젝트 구조
            </a>
          </li>
        </ul>
      </div>
    </div>
  );
}

export default App;

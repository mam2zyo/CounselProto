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
    if (mainContentRef.current) {
      mainContentRef.current.scrollTop = mainContentRef.current.scrollHeight;
    }
  }, [displayMessages]);

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

  // const loadConversations = async () => {
  //   try {
  //     const response = await fetchConversations();
  //     const updatedList = response.data;

  //     setConversations((prevConversations) => {
  //       // 이전 상태(prevConversations)를 기반으로 새로운 상태를 만듭니다.
  //       const mergedConversations = prevConversations.map((existingConv) => {
  //         // 서버에서 받은 목록에서 현재 대화와 ID가 같은 항목을 찾습니다.
  //         const updatedConv = updatedList.find((c) => c.id === existingConv.id);

  //         if (updatedConv) {
  //           // 찾았다면, 기존 대화의 모든 속성(...existingConv)은 유지하되,
  //           // title만 새로 받은 것으로 교체합니다.
  //           // 이렇게 하면 기존의 messages 배열이 보존됩니다.
  //           return { ...existingConv, title: updatedConv.title };
  //         }

  //         // 업데이트된 정보가 없는 다른 대화들은 그대로 둡니다.
  //         return existingConv;
  //       });

  //       // 병합된 결과로 상태를 업데이트합니다.
  //       return mergedConversations;
  //     });
  //   } catch (error) {
  //     console.error("대화 목록 로딩 실패:", error);
  //   }
  // };

  // 스크롤 자동 이동, 메시지 추가 시마다 실행
  useEffect(() => {
    if (mainContentRef.current) {
      mainContentRef.current.scrollTop = mainContentRef.current.scrollHeight;
    }
  }, [activeConversation?.messages]);

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

    //   // 스트리밍 시작
    //   streamChat(
    //     activeConversationId,
    //     userMessageText,
    //     (token) => {
    //       // onMessage: 스트리밍 데이터 수신 시
    //       setConversations((prev) =>
    //         prev.map((c) => {
    //           if (c.id === currentConvId) {
    //             const lastMessage = c.messages[c.messages.length - 1];
    //             // placeholder를 실제 AI 응답으로 교체하며 텍스트 누적
    //             if (lastMessage.id === aiMessageId) {
    //               const newText =
    //                 lastMessage.message === "..."
    //                   ? token
    //                   : lastMessage.message + token;
    //               const updatedMessages = [
    //                 ...c.messages.slice(0, -1),
    //                 { ...lastMessage, message: newText },
    //               ];
    //               return { ...c, messages: updatedMessages };
    //             }
    //           }
    //           return c;
    //         })
    //       );
    //     },
    //     (error) => {
    //       // onError: 에러 발생 시
    //       console.error("스트리밍 에러:", error);
    //       // 에러 메시지를 UI에 표시
    //       setConversations((prev) =>
    //         prev.map((c) => {
    //           if (c.id === currentConvId) {
    //             const updatedMessages = [
    //               ...c.messages.slice(0, -1),
    //               { ...aiMessagePlaceholder, message: "오류가 발생했습니다." },
    //             ];
    //             return { ...c, messages: updatedMessages };
    //           }
    //           return c;
    //         })
    //       );
    //       setIsSending(false);
    //     },
    //     () => {
    //       // onComplete: 스트림 완료 시
    //       setIsSending(false);
    //       if (isNewConversation) {
    //         loadConversations();
    //       }
    //     }
    //   );
    // };

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
        if (isNewConversation) {
          try {
            // 1. API를 직접 호출하여 데이터만 가져옵니다 (상태를 변경하지 않음).
            const response = await fetchConversations();
            const newConversationList = response.data;

            // 2. 백엔드는 최신순으로 정렬하므로, 첫 번째 항목이 방금 만든 대화입니다.
            const newConversationFromServer = newConversationList[0];

            if (!newConversationFromServer) {
              console.error("새 대화 정보를 서버에서 가져오지 못했습니다.");
              // 여기서 UI를 이전 상태로 되돌리는 로직을 추가할 수도 있습니다.
              setTempMessages([]);
              return;
            }

            // 3. 서버에서 받은 정보(id, title)와 프론트엔드가 가진 임시 메시지를 합쳐
            //    완전한 대화 객체를 만듭니다.
            const newFullConversation = {
              ...newConversationFromServer,
              messages: tempMessages,
            };

            // 4. 기존 conversations 상태의 맨 앞에 이 완전한 객체를 추가합니다.
            setConversations((prev) => [newFullConversation, ...prev]);

            // 5. 새 대화를 활성화합니다.
            setActiveConversationId(newConversationFromServer.id);

            // 6. 임시 메시지 상태를 비웁니다.
            setTempMessages([]);
          } catch (error) {
            console.error("새 대화 목록 갱신 실패:", error);
            // 에러 발생 시 사용자에게 알림을 주거나, 임시 메시지를 그대로 둘 수 있습니다.
          }
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

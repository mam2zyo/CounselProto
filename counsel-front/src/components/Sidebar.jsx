// src/components/Sidebar.jsx
import React from "react";

function Sidebar({
  conversations,
  setConversations,
  activeConversationId,
  setActiveConversationId,
  onNewConversation, // 새 대화 클릭 시 ChatPage에서 임시 대화 처리
}) {
  // 새 대화 클릭
  const handleNewConversation = () => {
    setActiveConversationId(null); // 새 대화 화면으로 전환
    if (typeof onNewConversation === "function") {
      onNewConversation(); // ChatPage에서 tempConversation 설정
    }
  };

  // 기존 대화 클릭
  const handleSelectConversation = (id) => {
    setActiveConversationId(id);
    localStorage.setItem("activeConversationId", id);
  };

  // 대화 삭제
  const deleteConversation = (id) => {
    setConversations((prev) => {
      const filtered = prev.filter((c) => c.id !== id);
      if (activeConversationId === id) {
        const nextId = filtered.length ? filtered[0].id : null;
        setActiveConversationId(nextId);
        if (nextId) localStorage.setItem("activeConversationId", nextId);
        else localStorage.removeItem("activeConversationId");
      }
      localStorage.setItem("conversations", JSON.stringify(filtered));
      return filtered;
    });
  };

  return (
    <div className="drawer-side border-r w-64">
      <label htmlFor="my-drawer" className="drawer-overlay"></label>
      <div className="menu p-4 overflow-y-auto w-64 bg-base-100">
        <h2 className="text-lg font-bold mb-2">대화 목록</h2>

        {/* 새 대화 버튼 */}
        <button
          className="btn btn-sm btn-primary w-full mb-2"
          onClick={handleNewConversation}
        >
          + 새 대화
        </button>

        {/* 기존 대화 목록 */}
        {conversations.map((c) => (
          <div
            key={c.id}
            className={`flex items-center justify-between mb-2 p-2 rounded-lg hover:bg-base-200 ${
              activeConversationId === c.id ? "bg-base-200 font-bold" : ""
            }`}
          >
            <button
              className="flex-1 text-left btn btn-ghost p-0 hover:bg-transparent"
              onClick={() => handleSelectConversation(c.id)}
            >
              {c.title}
            </button>

            <button
              className="btn btn-xs white ml-2"
              onClick={() => {
                if (confirm("이 대화를 삭제하시겠습니까?"))
                  deleteConversation(c.id);
              }}
            >
              🗑
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Sidebar;
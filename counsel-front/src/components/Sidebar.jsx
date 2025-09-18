// src/components/Sidebar.jsx
import { useState, useEffect } from "react";

function Sidebar({
  conversations,
  activeConversationId,
  editingId,
  onNewConversation,
  onSelectConversation,
  onDeleteConversation,
  onUpdateConversation,
  onStartEdit,
  onCancelEdit
}) {
  const [editedTitle, setEditedTitle] = useState("");

  useEffect(() => {
    if (editingId) {
      const conversationToEdit = conversations.find(c => c.id === editingId);
      if (conversationToEdit) {
        setEditedTitle(conversationToEdit.title || "");
      }
    }
  }, [editingId, conversations]);

  const handleUpdate = () => {
    if (!editedTitle.trim()) {
      alert("제목을 비워둘 수 없습니다.");
      return;
    }
    onUpdateConversation(editingId, { title: editedTitle });    
  }

  return (
    <div className="drawer-side border-r w-64">
      <label htmlFor="my-drawer" className="drawer-overlay"></label>
      <div className="menu p-4 overflow-y-auto w-64 bg-base-100">
        <h2 className="text-lg font-bold mb-2">대화 목록</h2>

        {/* 새 대화 버튼 */}
        <button
          className="btn btn-sm border border-gray-300 hover:bg-gray-100 w-full mb-0 flex items-center justify-start"
          onClick={onNewConversation}
        >
          <span className="text-base mr-2">💬</span> 새 대화
        </button>

        {/* 대화 목록 */}
        {Array.isArray(conversations) &&
          conversations.map((c) => (
            <div
              key={c.id}
              className={`flex items-center justify-between mb-0 p-0 rounded-lg hover:bg-base-200 ${
                activeConversationId === c.id ? "bg-base-200 font-bold" : ""
              }`}
            >
               {/* --- 수정 모드 UI --- */}
              {editingId === c.id ? (
                <div className="flex-1 flex items-center">
                  <input
                    type="text"
                    value={editedTitle}
                    onChange={(e) => setEditedTitle(e.target.value)}
                    className="input input-bordered input-xs w-full"
                    onKeyDown={(e) => e.key === 'Enter' && handleUpdate(c.id)}
                    autoFocus
                  />
                  <button className="btn btn-xs btn-ghost" onClick={() => handleUpdate(c.id)}>✓</button>
                  <button className="btn btn-xs btn-ghost" onClick={onCancelEdit}>✕</button>
                </div>
              ) : (
                // --- 일반 모드 UI ---
                <>
                  <button
                    className="flex-1 text-left btn btn-ghost p-0 hover:bg-transparent"
                    onClick={() => onSelectConversation(c.id)}
                  >
                    <span className="truncate">{c.title || "새로운 고민 상담"}</span>
                  </button>
                  <button
                    className="btn btn-xs btn-ghost ml-2"
                    onClick={() => onStartEdit(c.id)} // 수정 시작
                  >
                    ✏️
                  </button>
                  <button
                    className="btn btn-xs btn-ghost ml-1"
                    onClick={() => onDeleteConversation(c.id)}
                  >
                    🗑
                  </button>
                </>
              )}
            </div>
          ))}
      </div>
    </div>
  );
}

export default Sidebar;
            

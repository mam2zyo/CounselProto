// src/components/Sidebar.jsx
function Sidebar({
  conversations,
  activeConversationId,
  onNewConversation,
  onSelectConversation, // '선택' 이벤트를 알릴 함수 추가
  onDeleteConversation, // '삭제' 이벤트를 알릴 함수 추가
}) {
  // Sidebar는 더 이상 상태를 직접 변경하지 않습니다.
  // 모든 핸들러는 부모(ChatPage)에게 받은 함수를 호출하는 역할만 합니다.

  return (
    <div className="drawer-side border-r w-64">
      <label htmlFor="my-drawer" className="drawer-overlay"></label>
      <div className="menu p-4 overflow-y-auto w-64 bg-base-100">
        <h2 className="text-lg font-bold mb-2">대화 목록</h2>

        <button
          className="btn btn-sm btn-primary w-full mb-2"
          onClick={onNewConversation} // 부모에게 받은 함수 호출
        >
          + 새 대화
        </button>

        {/* conversations가 배열이 아닐 경우를 대비한 방어 코드 추가 */}
        {Array.isArray(conversations) &&
          conversations.map((c) => (
            <div
              key={c.id}
              className={`flex items-center justify-between mb-2 p-2 rounded-lg hover:bg-base-200 ${
                activeConversationId === c.id ? "bg-base-200 font-bold" : ""
              }`}
            >
              <button
                className="flex-1 text-left btn btn-ghost p-0 hover:bg-transparent"
                onClick={() => onSelectConversation(c.id)} // 부모에게 알림
              >
                {c.title || "새로운 고민 상담"} {/* title이 null일 경우 대비 */}
              </button>

              <button
                className="btn btn-xs btn-error ml-2"
                onClick={() => onDeleteConversation(c.id)} // 부모에게 알림
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

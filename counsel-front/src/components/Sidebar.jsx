// src/components/Sidebar.jsx
function Sidebar({
  conversations,
  activeConversationId,
  onNewConversation,
  onSelectConversation, // '선택' 이벤트
  onDeleteConversation, // '삭제' 이벤트
  onRenameConversation, // '이름 바꾸기' 이벤트
}) {
  return (
    <div className="drawer-side border-r w-64">
      <label htmlFor="my-drawer" className="drawer-overlay"></label>
      <div className="menu p-4 overflow-y-auto w-64 bg-base-100">
        <h2 className="text-lg font-bold mb-2">대화 목록</h2>

        {/* 새 대화 버튼 */}
        <button
          className="btn btn-sm border border-gray-300 hover:bg-gray-100 w-full mb-2 flex items-center justify-start"
          onClick={onNewConversation}
        >
          <span className="text-base mr-2">💬</span> 새 대화
        </button>

        {/* 대화 목록 */}
        {Array.isArray(conversations) &&
          conversations.map((c) => (
            <div
              key={c.id}
              className={`flex items-center justify-between mb-2 p-2 rounded-lg hover:bg-base-200 ${
                activeConversationId === c.id ? "bg-base-200 font-bold" : ""
              }`}
            >
              {/* 왼쪽: 대화 제목 */}
              <button
                className="flex-1 text-left p-0 hover:bg-transparent"
                onClick={() => onSelectConversation(c.id)}
              >
                {c.title || "새로운 고민 상담"}
              </button>

              {/* 오른쪽: 드롭다운 메뉴 */}
              <div className="dropdown dropdown-end ml-2">
                <label
                  tabIndex={0}
                  className="btn btn-xs bg-white border border-gray-300 hover:bg-gray-100 p-1"
                >
                  …  {/* 가로 방점 */}
                </label>
                <ul
                  tabIndex={0}
                  className="dropdown-content bg- menu p-1 shadow bg-base-100 rounded-box w-36 text-sm"
                >
                  <li>
                    <button
                      className="flex items-center gap-2 p-1 text-sm"
                      onClick={() => onRenameConversation(c.id)}
                    >
                      <span className="text-xs">✏️</span> 이름 바꾸기
                    </button>
                  </li>
                  <li>
                    <button
                      className="flex items-center gap-2 p-1 text-sm"
                      onClick={() => onDeleteConversation(c.id)}
                    >
                      <span className="text-xs">🗑</span> 삭제
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          ))}
      </div>
    </div>
  );
}

export default Sidebar;

function Sidebar({ conversations, setActiveConversationId }) {
  return (
    <div className="drawer-side">
      <label
        htmlFor="my-drawer"
        aria-label="close sidebar"
        className="drawer-overlay"
      ></label>
      <ul className="menu p-4 w-80 min-h-full bg-base-100 text-base-content">
        <li className="menu-title">대화 목록</li>

        {/* 새 대화 버튼 */}
        <li>
          <button
            className="btn btn-sm btn-primary w-full"
            onClick={() => setActiveConversationId(null)}
          >
            + 새 대화
          </button>
        </li>
      
        {conversations.map((c) => (
          <li key={c.id}>
            <button
              className="w-full text-left"
              onClick={() => setActiveConversationId(c.id)}
            >
              {c.title}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Sidebar;
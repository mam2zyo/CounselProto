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

// // src/components/Sidebar.jsx
// import React from "react";

// function Sidebar({
//   conversations,
//   activeConversationId,
//   onNewConversation,
//   onSelectConversation,
//   onDeleteConversation
// }) {

//   // 기존 코드는 로컬 스토리지에 직접 쓰기를 시도하였으나, 이제 api 기반으로 db에 작성이 필요
//   // // 새 대화 클릭
//   // const handleNewConversation = () => {
//   //   setActiveConversationId(null); // 새 대화 화면으로 전환
//   //   if (typeof onNewConversation === "function") {
//   //     onNewConversation(); // ChatPage에서 tempConversation 설정
//   //   }
//   // };

//   // // 기존 대화 클릭
//   // const handleSelectConversation = (id) => {
//   //   setActiveConversationId(id);
//   //   localStorage.setItem("activeConversationId", id);
//   // };

//   // // 대화 삭제
//   // const deleteConversation = (id) => {
//   //   setConversations((prev) => {
//   //     const filtered = prev.filter((c) => c.id !== id);
//   //     if (activeConversationId === id) {
//   //       const nextId = filtered.length ? filtered[0].id : null;
//   //       setActiveConversationId(nextId);
//   //       if (nextId) localStorage.setItem("activeConversationId", nextId);
//   //       else localStorage.removeItem("activeConversationId");
//   //     }
//   //     localStorage.setItem("conversations", JSON.stringify(filtered));
//   //     return filtered;
//   //   });
//   // };

//   return (
//     <div className="drawer-side border-r w-64">
//       <label htmlFor="my-drawer" className="drawer-overlay"></label>
//       <div className="menu p-4 overflow-y-auto w-64 bg-base-100">
//         <h2 className="text-lg font-bold mb-2">대화 목록</h2>

//         {/* 새 대화 버튼 */}
//         <button
//           className="btn btn-sm btn-primary w-full mb-2"
//           onClick={onNewConversation}
//         >
//           + 새 대화
//         </button>

//         {/* 기존 대화 목록 */}
//         {conversations.map((c) => (
//           <div
//             key={c.id}
//             className={`flex items-center justify-between mb-2 p-2 rounded-lg hover:bg-base-200 ${
//               activeConversationId === c.id ? "bg-base-200 font-bold" : ""
//             }`}
//           >
//             <button
//               className="flex-1 text-left btn btn-ghost p-0 hover:bg-transparent"
//               onClick={() => handleSelectConversation(c.id)}
//             >
//               {c.title}
//             </button>

//             <button
//               className="btn btn-xs btn-error ml-2"
//               onClick={() => {
//                 if (confirm("이 대화를 삭제하시겠습니까?"))
//                   deleteConversation(c.id);
//               }}
//             >
//               🗑
//             </button>
//           </div>
//         ))}
//       </div>
//     </div>
//   );
// }

// export default Sidebar;

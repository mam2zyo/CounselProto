import { Routes, Route } from "react-router-dom";
import ChatPage from "./pages/ChatPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import BoardPage from "./pages/BoardPage"; // ✅ 게시판 페이지 import

function App() {
  return (
    <Routes>
      <Route path="/" element={<ChatPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/board" element={<BoardPage />} /> {/* ✅ 게시판 라우트 추가 */}
    </Routes>
  );
}

export default App;

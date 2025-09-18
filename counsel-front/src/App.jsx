import { Routes, Route } from "react-router-dom";
import ChatPage from "./pages/ChatPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import BoardPage from "./pages/BoardPage";
import PostDetailPage from "./pages/PostDetailPage";


function App() {
  return (
    <Routes>
      <Route path="/" element={<ChatPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/board" element={<BoardPage />} />
        <Route path="/board/:postId" element={<PostDetailPage />} />
    </Routes>
  );
}

export default App;

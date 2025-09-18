// src/pages/BoardPage.jsx
import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getAllPosts } from "../api/board"

export default function BoardPage() {
  const { isLoggedIn } = useAuth();
  const [posts, setPosts] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        const response = await getAllPosts();
        setPosts(response.data);
      } catch (error) {
        console.error("게시글 목록 조회 실패", error);
        setPosts([]);
      }
    };

    if (isLoggedIn) {
      fetchPosts();
    } else {
      setPosts([]);
    }
  }, [isLoggedIn]);

  if (!isLoggedIn) {
    return (
      <div className="p-8">
        <p>로그인 후 게시판을 이용할 수 있습니다.</p>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">게시판</h1>
        <Link to="/board/new" className="btn btn-primary">
          새 글 작성
        </Link>
      </div>

      <div className="space-y-4">
        {posts.length > 0 ? (
          posts.map((post) => (
            <div
              key={post.postId}
              className="border rounded p-4 shadow-sm hover:shadow-md transition cursor-pointer"
              onClick={() => navigate(`/board/${post.postId}`)}
            >
              <h2 className="text-xl font-semibold text-blue-600">
                {post.title}
              </h2>
              <p className="text-sm text-gray-500 mt-1">
                작성자: {post.authorName}
              </p>
            </div>
          ))
        ) : (
          <p>게시글이 없습니다.</p>
        )}
      </div>
    </div>
  );
}
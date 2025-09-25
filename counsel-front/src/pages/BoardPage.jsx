// src/pages/BoardPage.jsx
import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getAllPosts } from "../api/board";

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
    <div className="p-8 max-w-5xl mx-auto">
      {/* 상단 헤더 */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">게시판</h1>
        <div className="flex gap-2">
          <Link to="/board/new" className="btn btn-ghost">
            새 글 작성
          </Link>
          <button className="btn btn-ghost" onClick={() => navigate("/")}>
            메인 화면
          </button>
        </div>
      </div>

      {/* 게시글 목록 - 네이버 카페 스타일 리스트 */}
      <div className="border rounded-lg overflow-hidden shadow-sm">
        {/* 헤더 */}
        <div className="bg-gray-100 flex px-4 py-2 text-sm font-semibold text-gray-600">
          <div className="w-5/12">제목</div>
          <div className="w-2/12">작성자</div>
          <div className="w-1/12 text-center">조회수</div>
          <div className="w-1/12 text-center">댓글</div>
          <div className="w-2/12 text-center">작성일/시간</div>
          <div className="w-2/12 text-right">상세</div>
        </div>

        {/* 게시글 */}
        {posts.length > 0 ? (
          posts.map((post) => (
            <div
              key={post.postId}
              className="flex px-4 py-3 items-center border-b hover:bg-gray-50 cursor-pointer transition"
              onClick={() => navigate(`/board/${post.postId}`)}
            >
              <div className="w-5/12 text-gray-800 font-medium">{post.title}</div>
              <div className="w-2/12 text-sm text-gray-600">익명</div>
              <div className="w-1/12 text-center text-gray-500 text-sm">{post.views ?? 0}</div>
              <div className="w-1/12 text-center text-gray-500 text-sm">
              
                {post.comments || 0}
              </div>
              <div className="w-2/12 text-center text-gray-500 text-sm">
                {post.createdAt
                  ? new Date(post.createdAt).toLocaleString([], {
                      year: "numeric",
                      month: "2-digit",
                      day: "2-digit",
                      hour: "2-digit",
                      minute: "2-digit",
                    })
                  : "-"}
              </div>
              <div className="w-2/12 text-right text-gray-400 text-xs">
                자세히 보기 →
              </div>
            </div>
          ))
        ) : (
          <p className="p-4 text-gray-500">게시글이 없습니다.</p>
        )}
      </div>
    </div>
  );
}

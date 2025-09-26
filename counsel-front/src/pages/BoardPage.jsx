import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getAllPosts } from "../api/board";

export default function BoardPage() {
  const { isLoggedIn } = useAuth();
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();

  // 게시글 가져오기 (페이지, 키워드)
  const fetchPosts = async (currentPage, searchKeyword = keyword) => {
    try {
      const response = await getAllPosts(currentPage, 10, searchKeyword);
      setPosts(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("게시글 목록 조회 실패", error);
      setPosts([]);
    }
  };

    // 로그인, 페이지, 키워드 변경 시 재조회
  useEffect(() => {
    if (isLoggedIn) {
      fetchPosts(page, keyword.trim());
    } else {
      setPosts([]);
    }
  }, [isLoggedIn, page, keyword]);

  const handlePrev = () => {
    if (page > 0) setPage(page - 1);
  };

  const handleNext = () => {
    if (page < totalPages - 1) setPage(page + 1);
  };

  
  // 검색 버튼 클릭 -> 페이지 0으로 초기화 + 재조회
  const handleSearch = () => {
    setPage(0);
    fetchPosts(0, keyword.trim());
  };

  // 엔터키로 검색
  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

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
        <h1 className="text-3xl font-bold">익명 게시판</h1>
        <div className="flex gap-2">
          <Link to="/board/new" className="btn btn-ghost">
            새 글 작성
          </Link>
          <button className="btn btn-ghost" onClick={() => navigate("/")}>
            메인 화면
          </button>
        </div>
      </div>

      {/* 게시글 목록 */}
      <div className="border rounded-lg overflow-hidden shadow-sm">
        {/* 헤더 */}
        <div className="bg-gray-100 flex px-4 py-2 text-sm font-semibold text-gray-600">
          <div className="w-6/12">제목</div>
          <div className="w-1/12 text-center">댓글</div>
          <div className="w-1/12 text-center">조회수</div>
          <div className="w-2/12 text-center">작성일/시간</div>
          <div className="w-2/12 text-right">상세</div>
        </div>

        {/* 게시글 목록 */}
        {posts.length > 0 ? (
          posts.map((post) => (
            <div
              key={post.postId}
              className="flex px-4 py-3 items-center border-b hover:bg-gray-50 cursor-pointer transition"
              onClick={() => navigate(`/board/${post.postId}`)}
            >
              <div className="w-6/12 text-gray-800 font-medium">{post.title}</div>
              <div className="w-1/12 text-center text-gray-500 text-sm">{post.comments || 0}</div>
              <div className="w-1/12 text-center text-gray-500 text-sm">{post.views || 0}</div>
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
              <div className="w-2/12 text-right text-gray-400 text-xs">자세히 보기 →</div>
            </div>
          ))
        ) : (
          <p className="p-4 text-gray-500">게시글이 없습니다.</p>
        )}
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center items-center mt-6 gap-4">
        <button className="btn btn-sm" onClick={handlePrev} disabled={page === 0}>
          ◀ 이전
        </button>
        <span className="text-sm text-gray-700">
          {page + 1} / {totalPages}
        </span>
        <button className="btn btn-sm" onClick={handleNext} disabled={page >= totalPages - 1}>
          다음 ▶
        </button>
      </div>

      {/* 검색 바 (하단에 위치) */}
      <div className="flex mt-8 items-center justify-center">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="제목, 내용 검색"
          className="input input-bordered w-full max-w-xs"
        />
        <button onClick={handleSearch} className="btn btn-primary ml-2">
          검색
        </button>
      </div>
    </div>
  );
}

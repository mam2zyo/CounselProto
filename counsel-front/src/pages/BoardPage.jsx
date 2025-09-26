import { useState, useEffect, useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getAllPosts } from "../api/board";
import apiClient from "../api";

export default function BoardPage() {
  const { isLoggedIn } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");
  const navigate = useNavigate();

  // 정렬 상태
  const [sortBy, setSortBy] = useState("latest"); // latest | views | comments
  const [order, setOrder] = useState("desc");      // desc | asc

  // ✅ 재사용 가능한 fetch 함수로 분리 (포커스/가시성 변화에도 재호출)
  const fetchPosts = useCallback(async () => {
    if (!isLoggedIn) {
      setPosts([]);
      return;
    }
    try {
      setLoading(true);
      setErr("");

      let data = [];

      if (sortBy === "latest") {
        // 최신순은 기존 API (/board)
        const response = await getAllPosts();
        data = Array.isArray(response.data) ? response.data : [];
      } else {
        // 조회/댓글 정렬은 서버에 sortBy 파라미터로 요청
        const { data: resp } = await apiClient.get("/board", { params: { sortBy } });
        data = Array.isArray(resp) ? resp : [];
      }

      // asc면 역순 처리(서버는 desc 기준으로 내려온다고 가정)
      if (order === "asc") data = [...data].reverse();

      setPosts(data);
    } catch (error) {
      console.error("게시글 목록 조회 실패", error);
      setErr(error?.response?.data?.message || "게시글 목록 조회 실패");
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [isLoggedIn, sortBy, order]);

  // 기본 로딩 + 정렬 변경 시 재조회
  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  // ✅ 상세 보고 돌아왔을 때(탭 포커스/가시성 복귀) 자동 갱신
  useEffect(() => {
    const onFocus = () => fetchPosts();
    const onVisibility = () => {
      if (document.visibilityState === "visible") fetchPosts();
    };
    window.addEventListener("focus", onFocus);
    document.addEventListener("visibilitychange", onVisibility);
    return () => {
      window.removeEventListener("focus", onFocus);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, [fetchPosts]);

  // 버튼 클릭 핸들러 (같은 버튼 누르면 asc/desc 토글)
  const handleSortClick = (key) => {
    if (sortBy === key) {
      setOrder((prev) => (prev === "desc" ? "asc" : "desc"));
    } else {
      setSortBy(key);
      setOrder("desc");
    }
  };

  // 활성 버튼에만 ▲/▼ 아이콘 표시
  const arrow = (key) => (sortBy === key ? (order === "desc" ? " ▼" : " ▲") : "");

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
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-3xl font-bold">게시판</h1>
        <div className="flex gap-2">
          <Link to="/board/new" className="btn btn-ghost">새 글 작성</Link>
          <button className="btn btn-ghost" onClick={() => navigate("/")}>메인 화면</button>
        </div>
      </div>

      {/* 정렬 버튼 */}
      <div className="flex items-center gap-2 mb-4">
        <span className="text-sm text-gray-600">
          현재 정렬:&nbsp;
          {sortBy === "latest"
            ? (order === "desc" ? "최신순" : "오래된순")
            : sortBy === "views"
              ? (order === "desc" ? "조회순" : "조회순")
              : (order === "desc" ? "댓글순" : "댓글순")}
        </span>

        <button
          onClick={() => handleSortClick("latest")}
          className={`px-3 py-1 border rounded ${sortBy === "latest" ? "font-semibold bg-gray-100" : ""}`}
        >
          최신순{arrow("latest")}
        </button>

        <button
          onClick={() => handleSortClick("views")}
          className={`px-3 py-1 border rounded ${sortBy === "views" ? "font-semibold bg-gray-100" : ""}`}
        >
          조회순{arrow("views")}
        </button>

        <button
          onClick={() => handleSortClick("comments")}
          className={`px-3 py-1 border rounded ${sortBy === "comments" ? "font-semibold bg-gray-100" : ""}`}
        >
          댓글순{arrow("comments")}
        </button>

        {loading && <span className="text-sm text-gray-500 ml-2">불러오는 중…</span>}
        {err && <span className="text-sm text-red-600 ml-2">에러: {err}</span>}
      </div>

      {/* 목록 */}
      <div className="border rounded-lg overflow-hidden shadow-sm">
        <div className="bg-gray-100 flex px-4 py-2 text-sm font-semibold text-gray-600">
          <div className="w-5/12">제목</div>
          <div className="w-2/12">작성자</div>
          <div className="w-1/12 text-center">댓글</div>
          <div className="w-1/12 text-center">조회수</div>
          <div className="w-2/12 text-center">작성일/시간</div>
          <div className="w-1/12 text-right">상세</div>
        </div>

        {posts.length > 0 ? (
          posts.map((post) => (
            <div
              key={post.postId ?? post.id}
              className="flex px-4 py-3 items-center border-b hover:bg-gray-50 cursor-pointer transition"
              onClick={() => navigate(`/board/${post.postId ?? post.id}`)}
            >
              <div className="w-5/12 text-gray-800 font-medium">{post.title}</div>
              <div className="w-2/12 text-sm text-gray-600">
                {post.writerName ?? post.authorName ?? "익명"}
              </div>
              <div className="w-1/12 text-center text-gray-500 text-sm">
                {post.comments ?? post.commentCount ?? 0}
              </div>
              <div className="w-1/12 text-center text-gray-500 text-sm">
                {post.views ?? post.viewCount ?? 0}
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
              <div className="w-1/12 text-right text-gray-400 text-xs">자세히 보기 →</div>
            </div>
          ))
        ) : (
          <p className="p-4 text-gray-500">게시글이 없습니다.</p>
        )}
      </div>
    </div>
  );
}

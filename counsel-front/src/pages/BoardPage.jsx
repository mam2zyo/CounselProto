import { useState, useEffect, useMemo } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getAllPosts } from "../api/board";

export default function BoardPage() {
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();

  // 목록/페이지/검색 상태
  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0); // 0-based
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  // 검색어 상태 분리: 입력 중인 검색어와 실제 검색어
  const [inputKeyword, setInputKeyword] = useState("");
  const [searchKeyword, setSearchKeyword] = useState("");

  // 정렬 상태
  const [sortBy, setSortBy] = useState("latest"); // latest | views | comments
  const [order, setOrder] = useState("desc"); // desc | asc (서버가 처리)

  // 로딩/에러
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  // 페이지 번호 버튼 배열 (최대 7개)
  const pageButtons = useMemo(() => {
    const maxButtons = 7;
    const pages = totalPages || 1;
    const current = page;
    const start = Math.max(
      0,
      Math.min(current - Math.floor(maxButtons / 2), pages - maxButtons)
    );
    const end = Math.min(pages - 1, start + maxButtons - 1);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }, [page, totalPages]);

  // 데이터 가져오기 (searchKeyword 기준으로 호출)
  useEffect(() => {
    if (!isLoggedIn) {
      setPosts([]);
      setTotalPages(1);
      return;
    }

    const fetch = async () => {
      setLoading(true);
      setErr("");
      try {
        console.log("검색어 요청: ", searchKeyword);  // 디버그용

          const { data } = await getAllPosts({
          keyword: searchKeyword,
          sortBy,
          direction: order,
          page,
          size,
        });

        const list = Array.isArray(data?.content) ? data.content : [];
        setPosts(list);
        setTotalPages(data?.totalPages ?? 1);
      } catch (e) {
        console.error("게시글 목록 조회 실패", e);
        setErr(e?.response?.data?.message || "게시글 목록 조회 실패");
        setPosts([]);
        setTotalPages(1);
      } finally {
        setLoading(false);
      }
    };

    fetch();
    window.scrollTo({ top: 0, behavior: "instant" });
  }, [isLoggedIn, searchKeyword, sortBy, order, page, size]);

  // 창 포커스/가시성 복귀 시 자동 갱신
  useEffect(() => {
    const onFocus = () => setPage((p) => p);
    const onVisibility = () => {
      if (document.visibilityState === "visible") setPage((p) => p);
    };
    window.addEventListener("focus", onFocus);
    document.addEventListener("visibilitychange", onVisibility);
    return () => {
      window.removeEventListener("focus", onFocus);
      document.removeEventListener("visibilitychange", onVisibility);
    };
  }, []);

  // 정렬 버튼 클릭 처리
  const handleSortClick = (key) => {
    if (sortBy === key) {
      setOrder((prev) => (prev === "desc" ? "asc" : "desc"));
    } else {
      setSortBy(key);
      setOrder("desc");
    }
    setPage(0);
  };
  const arrow = (key) =>
    sortBy === key ? (order === "desc" ? " ▼" : " ▲") : "";

  // 페이지 이동 함수
  const goToPage = (idx) => {
    if (idx < 0 || idx > totalPages - 1) return;
    setPage(idx);
  };
  const handlePrev = () => goToPage(page - 1);
  const handleNext = () => goToPage(page + 1);

  // 검색 input 변경 시 inputKeyword 상태만 변경
  const handleInputChange = (e) => {
    setInputKeyword(e.target.value);
  };

  // 검색 실행 (검색어 상태 변경 + 페이지 초기화)
  const handleSearch = () => {
    setPage(0);
    setSearchKeyword(inputKeyword.trim());
  };

  // 엔터키 눌렀을 때 검색 실행
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

      {/* 정렬/상태 */}
      <div className="flex items-center gap-2 mb-4">
        <span className="text-sm text-gray-600">
          현재 정렬:&nbsp;
          {sortBy === "latest"
            ? order === "desc"
              ? "최신순"
              : "오래된순"
            : sortBy === "views"
            ? "조회순"
            : "댓글순"}
        </span>

        <button
          onClick={() => handleSortClick("latest")}
          className={`px-3 py-1 border rounded ${
            sortBy === "latest" ? "font-semibold bg-gray-100" : ""
          }`}
        >
          최신순{arrow("latest")}
        </button>

        <button
          onClick={() => handleSortClick("views")}
          className={`px-3 py-1 border rounded ${
            sortBy === "views" ? "font-semibold bg-gray-100" : ""
          }`}
        >
          조회순{arrow("views")}
        </button>

        <button
          onClick={() => handleSortClick("comments")}
          className={`px-3 py-1 border rounded ${
            sortBy === "comments" ? "font-semibold bg-gray-100" : ""
          }`}
        >
          댓글순{arrow("comments")}
        </button>

        {loading && (
          <span className="text-sm text-gray-500 ml-2">불러오는 중…</span>
        )}
        {err && <span className="text-sm text-red-600 ml-2">에러: {err}</span>}
      </div>

      {/* 목록 헤더 */}
      <div className="border rounded-lg overflow-hidden shadow-sm">
        <div className="bg-gray-100 flex px-4 py-2 text-sm font-semibold text-gray-600">
          <div className="w-9/12 md:w-6/12 text-center">제목</div>
          <div className="w-2/12 text-center">댓글</div>
          <div className="w-1/12 text-center">조회수</div>
          <div className="hidden md:block w-3/12 text-center">작성일/시간</div>
        </div>

        {/* 게시글 목록 */}
        {posts.length > 0 ? (
          posts.map((post) => (
            <div
              key={post.postId ?? post.id}
              className="flex px-4 py-3 items-center border-b hover:bg-gray-50 cursor-pointer transition"
              onClick={() => navigate(`/board/${post.postId}`)}
            >
              <div className="w-9/12 md:w-6/12 px-2 text-gray-800 font-medium truncate">
                {post.title}
              </div>
              <div className="w-2/12 text-center text-gray-500 text-sm">
                {post.commentCount ?? 0}
              </div>
              <div className="w-1/12 text-center text-gray-500 text-sm">
                {post.viewCount ?? 0}
              </div>
              <div className="hidden md:block w-3/12 text-center text-gray-500 text-sm">
                {post.createdAt
                  ? new Date(post.createdAt).toLocaleString()
                  : ""}
              </div>
            </div>
          ))
        ) : (
          <div className="p-4 text-center text-gray-500">
            검색 결과가 없습니다.
          </div>
        )}
      </div>

      {/* 페이징 */}
      <div className="flex justify-center items-center gap-2 mt-4">
        <button
          className="btn btn-ghost"
          onClick={handlePrev}
          disabled={page === 0}
        >
          &lt; 이전
        </button>
        {pageButtons.map((idx) => (
          <button
            key={idx}
            className={`btn btn-ghost ${page === idx ? "btn-active" : ""}`}
            onClick={() => goToPage(idx)}
          >
            {idx + 1}
          </button>
        ))}
        <button
          className="btn btn-ghost"
          onClick={handleNext}
          disabled={page >= totalPages - 1}
        >
          다음 &gt;
        </button>
      </div>

      {/* 검색 */}
      <div className="mt-6 flex gap-2">
        <input
          type="text"
          className="input input-bordered flex-grow"
          placeholder="제목, 내용 검색"
          value={inputKeyword}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
        />
        <button className="btn btn-primary" onClick={handleSearch}>
          검색
        </button>
      </div>
    </div>
  );
}

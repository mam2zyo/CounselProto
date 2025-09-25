// src/pages/PostDetailPage.jsx
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import { useAuth } from "../contexts/AuthContext";
import CommentsBox from "../components/CommentsBox";

// axios 공통 설정 (이 파일 안에서만 사용)
const http = axios.create({
  baseURL: "/api",
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});
http.defaults.xsrfCookieName = "XSRF-TOKEN";
http.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

export default function PostDetailPage() {
  const { postId } = useParams(); // 'new' or 숫자
  const isNewPost = postId === "new";
  const navigate = useNavigate();
  const { user } = useAuth(); // ✅ 현재 로그인 유저 정보 (id, email 포함 가정)

  // 게시글 form state
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [attachments, setAttachments] = useState([]); // 기존 첨부 URL들
  const [newFiles, setNewFiles] = useState([]);       // 새로 추가할 파일
  const [deletedUrls, setDeletedUrls] = useState([]); // 삭제할 기존 파일 URL

  useEffect(() => {
    if (!isNewPost) fetchPost();
    else {
      setTitle("");
      setContent("");
      setAttachments([]);
      setNewFiles([]);
      setDeletedUrls([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postId]);

  // 게시글 단건 조회: GET /api/board/{postId}
  const fetchPost = async () => {
    try {
      const { data } = await http.get(`/board/${postId}`);
      setTitle(data.title ?? "");
      setContent(data.content ?? "");
      setAttachments(data.attachmentUrls ?? []);
    } catch (err) {
      console.error("게시글 조회 실패:", err);
      alert("게시글을 불러오지 못했습니다.");
      navigate("/board");
    }
  };

  const handleFileChange = (e) => {
    setNewFiles(Array.from(e.target.files));
  };

  const handleDeleteAttachment = (url) => {
    setAttachments((prev) => prev.filter((u) => u !== url));
    setDeletedUrls((prev) => [...prev, url]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const fd = new FormData();
      fd.append("title", title);
      fd.append("content", content);
      newFiles.forEach((file) => fd.append("newAttachments", file));
      deletedUrls.forEach((url) => fd.append("deletedAttachmentUrls", url));

      if (isNewPost) {
        // POST /api/board
        await axios.post("/api/board", fd, { withCredentials: true });
      } else {
        // PUT /api/board/{postId}
        await axios.put(`/api/board/${postId}`, fd, { withCredentials: true });
      }
      navigate("/board");
    } catch (err) {
      console.error("게시글 저장 실패:", err);
      alert("게시글 저장에 실패했습니다.");
    }
  };

  const handleDeletePost = async () => {
    if (!window.confirm("정말 이 게시글을 삭제하시겠습니까?")) return;
    try {
      // DELETE /api/board/{postId}
      await http.delete(`/board/${postId}`);
      navigate("/board");
    } catch (err) {
      console.error("게시글 삭제 실패:", err);
      alert(
        err?.response?.status === 403
          ? "삭제 권한이 없습니다. 로그인/권한을 확인하세요."
          : "게시글 삭제에 실패했습니다."
      );
    }
  };

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">
        {isNewPost ? "새 글 작성" : "게시글"}
      </h1>

      {/* 게시글 작성/수정 폼 */}
      <form onSubmit={handleSubmit} className="space-y-4 mb-12">
        <div>
          <label className="label">제목</label>
          <input
            type="text"
            className="input input-bordered w-full"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div>
          <label className="label">내용</label>
          <textarea
            className="textarea textarea-bordered w-full h-40"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
        </div>

        <div>
          <label className="label">첨부파일</label>

          {/* 기존 첨부파일 리스트 */}
          <div className="mb-2">
            {attachments.map((url) => (
              <div key={url} className="flex items-center gap-2">
                <a
                  href={url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="link link-primary"
                >
                  {url.split("/").pop()}
                </a>
                <button
                  type="button"
                  className="btn btn-xs btn-error"
                  onClick={() => handleDeleteAttachment(url)}
                >
                  삭제
                </button>
              </div>
            ))}
          </div>

          {/* 새 파일 선택 */}
          <input
            type="file"
            multiple
            onChange={handleFileChange}
            className="file-input file-input-bordered w-full"
          />
        </div>

        <div className="flex gap-2">
          <button type="submit" className="btn btn-primary">
            {isNewPost ? "작성 완료" : "수정 완료"}
          </button>

          {!isNewPost && (
            <button type="button" className="btn btn-error" onClick={handleDeletePost}>
              삭제
            </button>
          )}

          <button type="button" className="btn" onClick={() => navigate("/board")}>
            목록으로
          </button>
        </div>
      </form>

      {/* 댓글: 새 글 작성 모드가 아닐 때만 / 작성자만 수정·삭제 보이도록 currentUserId 전달 */}
      {!isNewPost && (
        <CommentsBox
          postId={Number(postId)}
          currentUserId={user?.id ?? null}
          currentUserEmail={user?.email ?? null}  // ✅ 이메일 전달
        />
      )}
    </div>
  );
}
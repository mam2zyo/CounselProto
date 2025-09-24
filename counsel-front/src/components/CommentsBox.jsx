// src/components/CommentsBox.jsx
import { useEffect, useState } from "react";
import axios from "axios";

// axios 공통 설정 (이 파일 안에서만 사용)
const http = axios.create({
  baseURL: "/api",
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});
http.defaults.xsrfCookieName = "XSRF-TOKEN";
http.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

export default function CommentsBox({
    
  postId,
  currentUserId = null,
  currentUserEmail = null,
}) 
{
    console.log("🟢 CommentsBox props:", { postId, currentUserId, currentUserEmail });

  const [comments, setComments] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState("");
  const [newComment, setNewComment] = useState("");
  const [editingId, setEditingId]   = useState(null);
  const [editingText, setEditingText] = useState("");

  // 내 댓글인지 확인 (숫자/문자 타입 차이 방지)
  const isMine = (c) =>
    c &&
    currentUserId != null &&
    String(c.authorId) === String(currentUserId);

  // 서버가 작성자명을 안 주는 구조이므로, "내 댓글"이면 이메일로 보강
  const patchIdentity = (c) => {
    if (!c) return c;
    return {
      ...c,
      writerName: c.writerName ?? (isMine(c) && currentUserEmail ? currentUserEmail : null),
    };
  };

  const load = async () => {
    try {
      setLoading(true);
      setError("");
      // GET /api/board/{postId}/comments
      const { data } = await http.get(`/board/${postId}/comments`);
      const list = Array.isArray(data) ? data : [];
      setComments(list.map(patchIdentity));
    } catch (err) {
      console.error("댓글 조회 실패:", err);
      setError(
        err?.response?.status === 403
          ? "댓글을 볼 권한이 없습니다. 로그인 상태를 확인하세요."
          : "댓글을 불러오지 못했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // postId나 로그인 정보가 바뀌면 재계산
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postId, currentUserId, currentUserEmail]);

  const onCreate = async (e) => {
    e.preventDefault();
    const content = newComment.trim();
    if (!content) return;
    try {
      // POST /api/board/{postId}/comments { content }
      const { data } = await http.post(`/board/${postId}/comments`, { content });
      // 방금 작성한 댓글은 authorId가 내 것 => 이메일로 표시명 보강
      const patched = patchIdentity({
        ...data,
        authorId: data?.authorId ?? currentUserId, // 혹시 누락 시 힌트
      });
      setComments((prev) => [...prev, patched]);
      setNewComment("");
    } catch (err) {
      console.error("댓글 작성 실패:", err);
      alert(
        err?.response?.status === 403
          ? "댓글 작성 권한이 없습니다. 로그인/권한을 확인하세요."
          : "댓글 작성에 실패했습니다."
      );
    }
  };

  const onStartEdit = (c) => {
    setEditingId(c.id);
    setEditingText(c.content);
  };

  const onCancelEdit = () => {
    setEditingId(null);
    setEditingText("");
  };

  const onUpdate = async (commentId) => {
    const content = editingText.trim();
    if (!content) return;
    try {
      // PUT /api/board/comments/{commentId} { content }
      const { data } = await http.put(`/board/comments/${commentId}`, { content });
      const patched = patchIdentity(data);
      setComments((prev) => prev.map((c) => (c.id === commentId ? patched : c)));
      onCancelEdit();
    } catch (err) {
      console.error("댓글 수정 실패:", err);
      alert(
        err?.response?.status === 403
          ? "본인이 작성한 댓글만 수정할 수 있습니다."
          : "댓글 수정에 실패했습니다."
      );
    }
  };

  const onDelete = async (commentId) => {
    if (!confirm("댓글을 삭제할까요?")) return;
    try {
      // DELETE /api/board/comments/{commentId}
      await http.delete(`/board/comments/${commentId}`);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (err) {
      console.error("댓글 삭제 실패:", err);
      alert(
        err?.response?.status === 403
          ? "본인이 작성한 댓글만 삭제할 수 있습니다."
          : "댓글 삭제에 실패했습니다."
      );
    }
  };

  const fmt = (dt) => {
    try {
      return new Date(dt).toLocaleString("ko-KR", { timeZone: "Asia/Seoul" });
    } catch {
      return dt;
    }
  };

  return (
    <section>
      <h2 className="text-2xl font-bold mb-4">댓글</h2>

      {loading && <div className="py-4 text-gray-500">불러오는 중...</div>}
      {error && <div className="py-2 text-red-600">{error}</div>}

      {!loading && !error && (
        <>
          {/* 댓글 목록 */}
          <div className="space-y-3 mb-6">
            {comments.length === 0 && (
              <div className="text-gray-500">첫 댓글을 남겨보세요.</div>
            )}
            {comments.map((c) => {
              const displayName =
                c.writerName ??
                (isMine(c) && currentUserEmail ? currentUserEmail : "익명");

              return (
                <div key={c.id} className="p-3 border rounded bg-base-200">
                  <div className="flex items-center justify-between">
                    <p className="font-semibold">{displayName}</p>
                    <p className="text-xs text-gray-500">{fmt(c.createdAt)}</p>
                  </div>

                  {editingId === c.id ? (
                    <div className="mt-2">
                      <textarea
                        className="textarea textarea-bordered w-full"
                        rows={3}
                        value={editingText}
                        onChange={(e) => setEditingText(e.target.value)}
                      />
                      <div className="mt-2 flex gap-2">
                        <button
                          className="btn btn-sm btn-primary"
                          onClick={() => onUpdate(c.id)}
                        >
                          수정 완료
                        </button>
                        <button className="btn btn-sm" onClick={onCancelEdit}>
                          취소
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <p className="mt-1 whitespace-pre-wrap">{c.content}</p>
                      <div className="mt-2 flex gap-2 text-sm">
                        {/* 서버가 최종 권한을 검사하므로, 프론트는 버튼을 항상 보여줘도 안전.
                            원하면 작성자 판별값을 내려받아 조건부로 노출하세요. */}
                        <button className="link" onClick={() => onStartEdit(c)}>
                          수정
                        </button>
                        <button
                          className="link text-red-600"
                          onClick={() => onDelete(c.id)}
                        >
                          삭제
                        </button>
                      </div>
                    </>
                  )}
                </div>
              );
            })}
          </div>

          {/* 댓글 작성 */}
          <form onSubmit={onCreate} className="flex gap-2">
            <input
              type="text"
              className="input input-bordered flex-grow"
              placeholder="댓글을 입력하세요"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
            />
            <button type="submit" className="btn btn-secondary">
              등록
            </button>
          </form>
        </>
      )}
    </section>
  );
}

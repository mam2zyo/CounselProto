// src/components/CommentsBox.jsx
import { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import {
  getCommentsByPostId,
  createComment,
  updateComment,
  deleteComment,
} from "../api/comment";

export default function CommentsBox({ postId }) {
  const { user } = useAuth(); // 현재 로그인된 사용자 정보
  useEffect(() => {
    console.log("CommentsBox User State:", user);
  }, [user]);

  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");

  // 수정 중인 댓글의 ID와 내용을 관리하는 상태
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingContent, setEditingContent] = useState("");

  useEffect(() => {
    fetchComments();
  }, [postId]);

  // 댓글 목록을 불러오는 함수
  const fetchComments = async () => {
    try {
      const res = await getCommentsByPostId(postId);
      setComments(res.data || []);
    } catch (error) {
      console.error("댓글 조회 실패", error);
    }
  };

  // 새 댓글 등록 처리
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      await createComment(postId, { content: newComment });
      setNewComment("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 작성 실패", error);
      alert("댓글 작성에 실패했습니다.");
    }
  };

  // 댓글 삭제 처리
  const handleDeleteComment = async (commentId) => {
    if (window.confirm("정말 이 댓글을 삭제하시겠습니까?")) {
      try {
        await deleteComment(commentId);
        fetchComments(); // 댓글 목록 새로고침
      } catch (error) {
        console.error("댓글 삭제 실패", error);
        alert("댓글 삭제에 실패했습니다.");
      }
    }
  };

  // 댓글 수정 모드로 전환
  const handleEditMode = (comment) => {
    setEditingCommentId(comment.id);
    setEditingContent(comment.content);
  };

  // 댓글 수정 내용 저장
  const handleUpdateComment = async (e) => {
    e.preventDefault();
    if (!editingContent.trim()) return;
    try {
      await updateComment(editingCommentId, { content: editingContent });
      setEditingCommentId(null);
      setEditingContent("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 수정 실패", error);
      alert("댓글 수정에 실패했습니다.");
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">댓글</h2>
      {/* 댓글 목록 */}
      <div className="space-y-3 mb-6">
        {comments.map((comment) => (
          <div key={comment.id} className="p-3 border rounded bg-base-200">
            {editingCommentId === comment.id ? (
              // 수정 모드일 때
              <form onSubmit={handleUpdateComment} className="flex gap-2">
                <input
                  type="text"
                  value={editingContent}
                  onChange={(e) => setEditingContent(e.target.value)}
                  className="input input-bordered flex-grow"
                />
                <button type="submit" className="btn btn-primary">
                  저장
                </button>
                <button
                  type="button"
                  onClick={() => setEditingCommentId(null)}
                  className="btn"
                >
                  취소
                </button>
              </form>
            ) : (
              // 일반 모드일 때
              <div>
                <p className="font-semibold">{comment.writerEmail}</p>
                <p className="whitespace-pre-wrap">{comment.content}</p>
                <div className="flex justify-between items-center mt-2">
                  <p className="text-xs text-gray-500">
                    {new Date(comment.createdAt).toLocaleString()}
                  </p>
                  {/* 현재 유저가 댓글 작성자일 경우 수정/삭제 버튼 표시 */}
                  {user && user.email === comment.writerEmail && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEditMode(comment)}
                        className="btn btn-xs"
                      >
                        수정
                      </button>
                      <button
                        onClick={() => handleDeleteComment(comment.id)}
                        className="btn btn-xs btn-error"
                      >
                        삭제
                      </button>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
      {/* 댓글 작성 폼 (로그인한 사용자에게만 보임) */}
      {user && (
        <form onSubmit={handleCommentSubmit} className="flex gap-2">
          <input
            type="text"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="댓글을 입력하세요"
            className="input input-bordered flex-grow"
          />
          <button type="submit" className="btn btn-secondary">
            등록
          </button>
        </form>
      )}
    </div>
  );
}

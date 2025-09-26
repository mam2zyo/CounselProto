// src/pages/PostDetailPage.jsx
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getPost, deletePost, createPost, updatePost } from "../api/board";
import { getCommentsByPostId, createComment } from "../api/comment"

// 이 페이지는 새 글 작성(/board/new)과 상세 보기/수정(/board/:postId)을 모두 처리합니다.
export default function PostDetailPage() {
  const { postId } = useParams(); // URL에서 postId 가져오기. 'new'일 수도 있음
  const isNewPost = postId === "new";
  const navigate = useNavigate();
  const { user } = useAuth(); // 현재 로그인된 사용자 정보 (id, userName 등 포함 가정)

  // Form State
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [attachments, setAttachments] = useState([]); // 기존 첨부파일 목록
  const [newFiles, setNewFiles] = useState([]); // 새로 추가할 파일
  const [deletedUrls, setDeletedUrls] = useState([]); // 삭제할 첨부파일 URL

  // Comment State
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");

  useEffect(() => {
    if (!isNewPost) {
      fetchPostAndComments();
    }
  }, [postId]);

  const fetchPostAndComments = async () => {
    try {
      const [postRes, commentsRes] = await Promise.all([
        getPost(postId),
        getCommentsByPostId(postId),
      ]);
      const postData = postRes.data;
      setTitle(postData.title);
      setContent(postData.content);
      setAttachments(postData.attachmentUrls || []);
      setComments(commentsRes.data || []);
    } catch (error) {
      console.error("게시글 또는 댓글 조회 실패", error);
      navigate("/board");
    }
  };

  const handleFileChange = (e) => {
    setNewFiles(Array.from(e.target.files));
  };

  const handleDeleteAttachment = (url) => {
    setAttachments(attachments.filter((att) => att !== url));
    setDeletedUrls([...deletedUrls, url]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append("title", title);
    formData.append("content", content);
    newFiles.forEach((file) => formData.append("newAttachments", file));
    deletedUrls.forEach((url) => formData.append("deletedAttachmentUrls", url));

    try {
      if (isNewPost) {
        await createPost(formData);
      } else {
        await updatePost(postId, formData);
      }
      navigate("/board");
    } catch (error) {
      console.error("게시글 저장 실패", error);
      alert("게시글 저장에 실패했습니다.");
    }
  };

  const handleDeletePost = async () => {
    if (window.confirm("정말 이 게시글을 삭제하시겠습니까?")) {
      try {
        await deletePost(postId);
        navigate("/board");
      } catch (error) {
        console.error("게시글 삭제 실패", error);
        alert("게시글 삭제에 실패했습니다.");
      }
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      await createComment(postId, { content: newComment });
      setNewComment("");
      fetchPostAndComments(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 작성 실패", error);
    }
  };
  
  // (댓글 수정/삭제 기능은 이 컴포넌트가 너무 커지므로 CommentSection.jsx 등으로 분리하는 것이 좋습니다)

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
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="input input-bordered w-full"
            required
          />
        </div>
        <div>
          <label className="label">내용</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="textarea textarea-bordered w-full h-40"
            required
          />
        </div>
        <div>
          <label className="label">첨부파일</label>
          {/* 기존 파일 목록 */}
          <div className="mb-2">
            {attachments.map((url) => (
              <div key={url} className="flex items-center gap-2">
                <a href={url} target="_blank" rel="noopener noreferrer" className="link link-primary">
                  {url.split("/").pop()}
                </a>
                <button type="button" onClick={() => handleDeleteAttachment(url)} className="btn btn-xs btn-error">
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
            <button type="button" onClick={handleDeletePost} className="btn btn-error">
              삭제
            </button>
          )}
          <button type="button" onClick={() => navigate("/board")} className="btn">
            목록으로
          </button>
        </div>
      </form>

      {/* 댓글 섹션 (새 글 작성이 아닐 때만 표시) */}
      {!isNewPost && (
        <div>
          <h2 className="text-2xl font-bold mb-4">댓글</h2>
          {/* 댓글 목록 */}
          <div className="space-y-3 mb-6">
            {comments.map((comment) => (
              <div key={comment.id} className="p-3 border rounded bg-base-200">
                <p className="font-semibold">{comment.writerName}</p>
                <p>{comment.content}</p>
                <p className="text-xs text-gray-500">
                  {new Date(comment.createdAt).toLocaleString()}
                </p>
                {/* 현재 유저가 댓글 작성자일 경우 수정/삭제 버튼 표시 */}
              </div>
            ))}
          </div>
          {/* 댓글 작성 폼 */}
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
        </div>
      )}
    </div>
  );
}
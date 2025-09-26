import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { getPost, deletePost, createPost, updatePost } from "../api/board";
import { getCommentsByPostId, createComment } from "../api/comment";

export default function PostDetailPage() {
  const { postId } = useParams();
  const isNewPost = postId === "new";
  const navigate = useNavigate();
  const { user } = useAuth();

  const [post, setPost] = useState(null); // 전체 post 객체 (조회수 포함)
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [attachments, setAttachments] = useState([]);
  const [newFiles, setNewFiles] = useState([]);
  const [deletedUrls, setDeletedUrls] = useState([]);

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
      console.log("현재 조회수:", postData.views);
      setPost(postData); // 전체 객체 저장 (조회수 포함)
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
      fetchPostAndComments(); // 댓글 새로고침
    } catch (error) {
      console.error("댓글 작성 실패", error);
    }
  };

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <h1 className="text-3xl font-bold mb-4">
        {isNewPost ? "새 글 작성" : "게시글"}
      </h1>

      {!isNewPost && post && (
        <p className="text-sm text-gray-500 mb-4">
          👁️ 조회수: <strong>{post.views}</strong>
        </p>
      )}

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

      {!isNewPost && (
        <div>
          <h2 className="text-2xl font-bold mb-4">댓글</h2>
          <div className="space-y-3 mb-6">
            {comments.map((comment) => (
              <div key={comment.id} className="p-3 border rounded bg-base-200">
                <p className="font-semibold">{comment.writerName}</p>
                <p>{comment.content}</p>
                <p className="text-xs text-gray-500">
                  {new Date(comment.createdAt).toLocaleString()}
                </p>
              </div>
            ))}
          </div>
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

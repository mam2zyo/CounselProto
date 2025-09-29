import React from "react";
import { useNavigate } from "react-router-dom";
import { FiEye, FiMessageSquare } from "react-icons/fi";
import { FaUserCircle } from "react-icons/fa";

export default function PostViewer({
  post,
  isAuthor,
  onEdit,
  onDelete,
  liked,
  likeCount,
  onToggleLike,
}) {
  const navigate = useNavigate();

  const formattedDate = post.createdAt
    ? new Date(post.createdAt)
        .toLocaleString("ko-KR", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        })
        .replace(/\. /g, ".")
        .slice(0, -1)
    : "";

  return (
    <div className="bg-white shadow-md rounded-lg p-6 md:p-8">
      <div className="mb-6">
        <h1 className="text-3xl md:text-4xl font-bold break-words mb-4">
          {post.title}
        </h1>

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <FaUserCircle className="text-gray-400" size={40} />
            <div>
              <span className="font-semibold text-gray-800">익명</span>
              <div className="text-xs text-gray-500">{formattedDate}</div>
            </div>
          </div>

          <div className="flex items-center gap-4 text-sm text-gray-500">
            <span className="flex items-center gap-1.5">
              <FiEye /> {post.viewCount ?? 0}
            </span>
            <span className="flex items-center gap-1.5">
              <FiMessageSquare /> {post.commentCount ?? 0}
            </span>
          </div>
        </div>
      </div>

      <hr className="my-6 border-gray-200" />

      <div className="mb-8 min-h-[200px] text-gray-800 leading-relaxed break-words whitespace-pre-wrap">
        {post.content}
      </div>

      {post.attachmentUrls && post.attachmentUrls.length > 0 && (
        <div className="mb-8 p-4 bg-gray-50 rounded-md border">
          <h3 className="text-md font-semibold mb-3">첨부파일</h3>
          <div className="space-y-2">
            {post.attachmentUrls.map((url) => (
              <div key={url} className="truncate">
                <a
                  href={url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="link link-hover"
                >
                  {decodeURIComponent(url.split("/").pop())}
                </a>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="flex justify-end gap-2 mt-10 items-center">
        <button
          onClick={onToggleLike}
          className={`btn btn-sm sm:btn-md ${
            liked ? "btn-primary" : "btn-outline"
          }`}
          aria-pressed={liked}
          aria-label={liked ? "좋아요 취소" : "좋아요"}
        >
          {liked ? "❤️ 좋아요" : "🤍 좋아요"} {likeCount ?? 0}
        </button>

        <button onClick={() => navigate("/board")} className="btn btn-sm sm:btn-md">
          목록
        </button>

        {isAuthor && (
          <>
            <button onClick={onEdit} className="btn btn-outline btn-sm sm:btn-md">
              수정
            </button>
            <button onClick={onDelete} className="btn btn-error btn-sm sm:btn-md">
              삭제
            </button>
          </>
        )}
      </div>
    </div>
  );
}

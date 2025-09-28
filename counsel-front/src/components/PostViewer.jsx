// src/components/PostViewer.jsx
import { useNavigate } from "react-router-dom";
// 아이콘 라이브러리 설치 후 import (설치: npm install react-icons)
import { FiEye, FiMessageSquare } from "react-icons/fi";

export default function PostViewer({ post, isAuthor, onEdit, onDelete }) {
  const navigate = useNavigate();

  // 날짜 형식을 "YYYY.MM.DD HH:MM" 형태로 보기 좋게 변경
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
    <div>
      {/* --- 1. 헤더 영역: 제목과 부가 정보 --- */}
      <div className="mb-6 pb-4 border-b">
        <h1 className="text-3xl md:text-4xl font-bold mb-3 break-words">
          {post.title}
        </h1>
        <div className="flex items-center justify-between text-sm text-gray-500">
          {/* 날짜 정보 */}
          <span>{formattedDate}</span>
          {/* 조회수와 댓글 수 (아이콘과 함께) */}
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1">
              <FiEye /> {post.viewCount ?? 0}
            </span>
            <span className="flex items-center gap-1">
              <FiMessageSquare /> {post.commentCount ?? 0}
            </span>
          </div>
        </div>
      </div>

      {/* --- 2. 본문 영역 --- */}
      <div className="mb-8 min-h-[200px] text-gray-800 leading-relaxed break-words whitespace-pre-wrap">
        {post.content}
      </div>

      {/* --- 3. 첨부파일 영역 (구조는 유지, 여백 추가) --- */}
      {post.attachmentUrls && post.attachmentUrls.length > 0 && (
        <div className="mb-8 p-4 bg-gray-50 rounded-md border">
          <h3 className="text-md font-semibold mb-3">첨부파일</h3>
          <div className="space-y-2">
            {post.attachmentUrls.map((url) => (
              <div key={url}>
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

      {/* --- 4. 액션 버튼 영역 (여백 추가) --- */}
      <div className="flex gap-2 mt-10">
        <button onClick={() => navigate("/board")} className="btn">
          목록으로
        </button>
        {isAuthor && (
          <>
            <button onClick={onEdit} className="btn btn-outline">
              수정
            </button>
            <button onClick={onDelete} className="btn btn-error">
              삭제
            </button>
          </>
        )}
      </div>
    </div>
  );
}

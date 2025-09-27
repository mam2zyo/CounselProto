// src/pages/PostDetailPage.jsx
import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getPost,
  deletePost,
  createPost,
  updatePost,
  recordView,
} from "../api/board";
import CommentsBox from "../components/CommentsBox";

export default function PostDetailPage() {
  const { postId } = useParams();
  const isNewPost = postId === "new";
  const navigate = useNavigate();

  // Form State
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [attachments, setAttachments] = useState([]);
  const [newFiles, setNewFiles] = useState([]);
  const [deletedUrls, setDeletedUrls] = useState([]);

  // React 18 StrictMode에서 중복 fetch를 방지하기 위한 Ref
  const fetchGuard = useRef(false);

  // 게시글 데이터 조회와 조회수 기록을 하나의 useEffect로 처리
  useEffect(() => {
    // 새 글 작성이거나, 이미 fetch가 실행되었다면 중단 (StrictMode 대응)
    if (isNewPost || fetchGuard.current) {
      return;
    }
    fetchGuard.current = true;

    const fetchPostAndRecordView = async () => {
      try {
        // 1. 게시글 데이터 가져오기
        const res = await getPost(postId);
        const postData = res.data;
        setTitle(postData.title);
        setContent(postData.content);
        setAttachments(postData.attachmentUrls || []);

        recordView(postId).catch((err) => {
          console.warn("조회수 기록에 실패했습니다.", err);
        });
      } catch (error) {
        console.error("게시글 조회 실패", error);
        alert("게시글을 불러오는 데 실패했습니다.");
        navigate("/board");
      }
    };

    fetchPostAndRecordView();

    // 컴포넌트가 unmount될 때 guard를 초기화하여
    // 다음에 다른 게시물로 이동했을 때 fetch가 정상 실행되도록 함
    return () => {
      fetchGuard.current = false;
    };
  }, [postId, isNewPost, navigate]);

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

    const filesKey = isNewPost ? "attachments" : "newAttachments";
    newFiles.forEach((file) => formData.append(filesKey, file));

    if (!isNewPost) {
      deletedUrls.forEach((url) =>
        formData.append("deletedAttachmentUrls", url)
      );
    }

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
                  onClick={() => handleDeleteAttachment(url)}
                  className="btn btn-xs btn-error"
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
            <button
              type="button"
              onClick={() => handleDeletePost()}
              className="btn btn-error"
            >
              삭제
            </button>
          )}
          <button
            type="button"
            onClick={() => navigate("/board")}
            className="btn"
          >
            목록으로
          </button>
        </div>
      </form>

      {/* 댓글 섹션 (새 글 작성이 아닐 때만 표시) */}
      {!isNewPost && <CommentsBox postId={postId} />}
    </div>
  );
}

// // src/pages/PostDetailPage.jsx
// import { useState, useEffect, useRef } from "react";
// import { useParams, useNavigate } from "react-router-dom";
// // import { useAuth } from "../contexts/AuthContext";  // 어디서 사용하는지 확인 필요
// import { getPost, deletePost, createPost, updatePost, recordView } from "../api/board"; // ✅ recordView 추가
// import CommentsBox from "../components/CommentsBox";

// // 이 페이지는 새 글 작성(/board/new)과 상세 보기/수정(/board/:postId)을 모두 처리합니다.
// export default function PostDetailPage() {
//   const { postId } = useParams(); // URL에서 postId 가져오기. 'new'일 수도 있음
//   const isNewPost = postId === "new";
//   const navigate = useNavigate();
//   // const { user } = useAuth(); // 현재 로그인된 사용자 정보 (id, userName 등 포함 가정)

//   // Form State
//   const [title, setTitle] = useState("");
//   const [content, setContent] = useState("");
//   const [attachments, setAttachments] = useState([]); // 기존 첨부파일 목록
//   const [newFiles, setNewFiles] = useState([]); // 새로 추가할 파일
//   const [deletedUrls, setDeletedUrls] = useState([]); // 삭제할 첨부파일 URL

//   // ✅ postId 별 1회만 fetch되도록 가드
//   const fetchedForPostIdRef = useRef(null);

//   // ✅ 상세 진입 시 "유니크 조회" 1회만 기록 (백엔드가 유니크 가드)
//   const recordedViewRef = useRef(false);
//   useEffect(() => {
//     if (isNewPost || !postId) return;

//     // 같은 렌더 사이클 중복 방지
//     if (recordedViewRef.current) return;
//     recordedViewRef.current = true;

//     // 탭 세션 기준 1회만 기록
//     const key = `viewed:${postId}`;
//     if (!sessionStorage.getItem(key)) {
//       recordView(postId).finally(() => sessionStorage.setItem(key, "1"));
//     }
//   }, [postId, isNewPost]);

//   useEffect(() => {
//     if (!isNewPost) {
//       fetchPost();
//     }
//   }, [postId, isNewPost]);

//   useEffect(() => {
//     if (isNewPost) return;

//     // 같은 postId로 이미 가져왔으면 재호출 방지 (React 18 StrictMode 대응)
//     if (fetchedForPostIdRef.current === postId) return;
//     fetchedForPostIdRef.current = postId;

//     // fetchPostAndComments(); // ❌ 댓글 로직은 CommentsBox로 이관
//     fetchPost(); // ✅ 게시글만 조회 (댓글은 CommentsBox가 맡음)
//   }, [postId, isNewPost]);

//   const fetchPost = async () => {
//     try {
//       const res = await getPost(postId);
//       const postData = res.data;
//       setTitle(postData.title);
//       setContent(postData.content);
//       setAttachments(postData.attachmentUrls || []);
//     } catch (error) {
//       console.error("게시글 조회 실패", error);
//       navigate("/board");
//     }
//   };

//   const handleFileChange = (e) => {
//     setNewFiles(Array.from(e.target.files));
//   };

//   const handleDeleteAttachment = (url) => {
//     setAttachments(attachments.filter((att) => att !== url));
//     setDeletedUrls([...deletedUrls, url]);
//   };

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     const formData = new FormData();
//     formData.append("title", title);
//     formData.append("content", content);

//     // 백엔드 PostRequest, PostUpdateRequest DTO의 필드명과 일치 필요
//     // create: attachments, update: newAttachments
//     const filesKey = isNewPost ? "attachments" : "newAttachments";
//     newFiles.forEach((file) => formData.append(filesKey, file));

//     // 수정시만 제공
//     if (!isNewPost) {
//       deletedUrls.forEach((url) =>
//         formData.append("deletedAttachmentUrls", url)
//       );
//     }

//     try {
//       if (isNewPost) {
//         await createPost(formData);
//       } else {
//         await updatePost(postId, formData);
//       }
//       navigate("/board");
//     } catch (error) {
//       console.error("게시글 저장 실패", error);
//       alert("게시글 저장에 실패했습니다.");
//     }
//   };

//   const handleDeletePost = async () => {
//     if (window.confirm("정말 이 게시글을 삭제하시겠습니까?")) {
//       try {
//         await deletePost(postId);
//         navigate("/board");
//       } catch (error) {
//         console.error("게시글 삭제 실패", error);
//         alert("게시글 삭제에 실패했습니다.");
//       }
//     }
//   };

//   return (
//     <div className="p-8 max-w-4xl mx-auto">
//       <h1 className="text-3xl font-bold mb-6">
//         {isNewPost ? "새 글 작성" : "게시글"}
//       </h1>

//       {/* 게시글 작성/수정 폼 */}
//       <form onSubmit={handleSubmit} className="space-y-4 mb-12">
//         <div>
//           <label className="label">제목</label>
//           <input
//             type="text"
//             value={title}
//             onChange={(e) => setTitle(e.target.value)}
//             className="input input-bordered w-full"
//             required
//           />
//         </div>
//         <div>
//           <label className="label">내용</label>
//           <textarea
//             value={content}
//             onChange={(e) => setContent(e.target.value)}
//             className="textarea textarea-bordered w-full h-40"
//             required
//           />
//         </div>
//         <div>
//           <label className="label">첨부파일</label>
//           {/* 기존 파일 목록 */}
//           <div className="mb-2">
//             {attachments.map((url) => (
//               <div key={url} className="flex items-center gap-2">
//                 <a
//                   href={url}
//                   target="_blank"
//                   rel="noopener noreferrer"
//                   className="link link-primary"
//                 >
//                   {url.split("/").pop()}
//                 </a>
//                 <button
//                   type="button"
//                   onClick={() => handleDeleteAttachment(url)}
//                   className="btn btn-xs btn-error"
//                 >
//                   삭제
//                 </button>
//               </div>
//             ))}
//           </div>
//           {/* 새 파일 선택 */}
//           <input
//             type="file"
//             multiple
//             onChange={handleFileChange}
//             className="file-input file-input-bordered w-full"
//           />
//         </div>
//         <div className="flex gap-2">
//           <button type="submit" className="btn btn-primary">
//             {isNewPost ? "작성 완료" : "수정 완료"}
//           </button>
//           {!isNewPost && (
//             <button
//               type="button"
//               onClick={() => handleDeletePost()}
//               className="btn btn-error"
//             >
//               삭제
//             </button>
//           )}
//           <button
//             type="button"
//             onClick={() => navigate("/board")}
//             className="btn"
//           >
//             목록으로
//           </button>
//         </div>
//       </form>

//       {/* 댓글 섹션 (새 글 작성이 아닐 때만 표시) */}
//       {!isNewPost && <CommentsBox postId={postId} />}
//     </div>
//   );
// }

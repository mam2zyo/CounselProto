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
import PostViewer from "../components/PostViewer"; // ✅ Viewer 임포트
import PostEditor from "../components/PostEditor"; // ✅ Editor 임포트
import { useAuth } from "../contexts/AuthContext";

export default function PostDetailPage() {
  const { postId } = useParams();
  const isNewPost = postId === "new";
  const navigate = useNavigate();
  const { user } = useAuth();
  // 모드 관리 상태 추가
  const [isEditing, setIsEditing] = useState(isNewPost);
  // 게시글 데이터 전체를 하나의 상태로 관리
  const [post, setPost] = useState({
    title: "",
    content: "",
    attachmentUrls: [],
    authorId: null,
  });
  const [isAuthor, setIsAuthor] = useState(false);
  const [isLoading, setIsLoading] = useState(!isNewPost);
  const initiatedFetchPostId = useRef(null);
  useEffect(() => {
    if (isNewPost) return;

    if (initiatedFetchPostId.current === postId) return;
    initiatedFetchPostId.current = postId;

    const fetchPostAndRecordView = async () => {
      try {
        const response = await getPost(postId);
        const postData = response.data;
        setPost(postData); // 받아온 데이터로 post 상태 업데이트

        if (user && postData.authorId === user.id) {
          setIsAuthor(true);
        }
        recordView(postId).catch((err) =>
          console.warn("조회수 기록 실패", err)
        );
      } catch (error) {
        console.error("게시글 조회 실패", error);
        alert("게시글을 불러오는 데 실패했습니다.");
        navigate("/board");
      } finally {
        setIsLoading(false);
      }
    };

    fetchPostAndRecordView();
  }, [postId, isNewPost, navigate, user]);

  // Editor로부터 데이터를 받아 API 요청을 보내는 핸들러
  const handleSave = async (editedPost, newFiles, deletedUrls) => {
    const formData = new FormData();
    formData.append("title", editedPost.title);
    formData.append("content", editedPost.content);

    try {
      if (isNewPost) {
        newFiles.forEach((file) => formData.append("attachments", file));
        await createPost(formData);
        navigate("/board"); // 새 글 작성 성공 시 목록으로 이동
      } else {
        newFiles.forEach((file) => formData.append("newAttachments", file));
        deletedUrls.forEach((url) =>
          formData.append("deletedAttachmentUrls", url)
        );
        const response = await updatePost(postId, formData);

        // 수정 성공 시, 서버로부터 받은 최신 데이터로 상태를 업데이트하고 읽기 모드로 전환
        setPost(response.data);
        setIsEditing(false);
      }
    } catch (error) {
      console.error("게시글 저장 실패", error);
      alert("게시글 저장에 실패했습니다.");
    }
  };
  const handleDelete = async () => {
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
  // 편집 취소 핸들러
  const handleCancel = () => {
    if (isNewPost) {
      navigate("/board"); // 새 글 작성 중 취소는 목록으로
    } else {
      setIsEditing(false); // 수정 중 취소는 읽기 모드로
    }
  };
  if (isLoading) {
    return <div className="p-8">게시글을 불러오는 중입니다...</div>;
  }
  return (
    <div className="p-8 max-w-4xl mx-auto">
      {/* isEditing 상태에 따라 Viewer 또는 Editor를 조건부 렌더링 */}
      {isEditing ? (
        <PostEditor
          initialPost={{
            // Editor에 초기 데이터 전달
            title: post.title,
            content: post.content,
            attachments: post.attachmentUrls,
          }}
          onSave={handleSave}
          onCancel={handleCancel}
          isNewPost={isNewPost}
        />
      ) : (
        <PostViewer
          post={post}
          isAuthor={isAuthor}
          onEdit={() => setIsEditing(true)} // "수정" 버튼 누르면 편집 모드로 전환
          onDelete={handleDelete}
        />
      )}

      {/* 댓글은 모드와 상관없이 항상 표시 */}
      {!isNewPost && <CommentsBox postId={postId} />}
    </div>
  );
}

// // src/pages/PostDetailPage.jsx
// import { useState, useEffect, useRef } from "react";
// import { useParams, useNavigate } from "react-router-dom";
// import { useAuth } from "../contexts/AuthContext";
// import CommentsBox from "../components/CommentsBox";
// import AttachmentBox from "../components/AttachmentBox";
// import {
//   getPost,
//   deletePost,
//   createPost,
//   updatePost,
//   recordView,
// } from "../api/board";

// export default function PostDetailPage() {
//   const { postId } = useParams();
//   const isNewPost = postId === "new";
//   const navigate = useNavigate();
//   const { user } = useAuth();

//   // Form State
//   const [title, setTitle] = useState("");
//   const [content, setContent] = useState("");
//   const [attachments, setAttachments] = useState([]);
//   const [newFiles, setNewFiles] = useState([]);
//   const [deletedUrls, setDeletedUrls] = useState([]);

//   // 작성자 여부를 관리할 상태 추가
//   const [isAuthor, setIsAuthor] = useState(false);
//   const [isLoading, setIsLoading] = useState(!isNewPost);

//   // StrictMode 중복 호출 방지를 위해, fetch가 시작된 postId를 기록하는 Ref
//   const initiatedFetchPostId = useRef(null);

//   // 게시글 데이터 조회와 조회수 기록을 하나의 useEffect로 처리
//   useEffect(() => {
//     if (isNewPost) {
//       setIsLoading(false);
//       return;
//     }

//     // 현재 postId에 대한 API 호출이 이미 "시작"되었다면 중복 실행을 방지합니다.
//     if (initiatedFetchPostId.current === postId) {
//       return;
//     }

//     initiatedFetchPostId.current = postId;

//     const fetchPostAndRecordView = async () => {
//       try {
//         // 1. 게시글 데이터 가져오기
//         const response = await getPost(postId);
//         const postData = response.data;
//         setTitle(postData.title);
//         setContent(postData.content);
//         setAttachments(postData.attachmentUrls || []);

//         // 현재 사용자와 게시글 작성자 ID 비교
//         if (user && postData.authorId === user.id) {
//           setIsAuthor(true);
//         }

//         recordView(postId).catch((err) => {
//           console.warn("조회수 기록에 실패했습니다.", err);
//         });
//       } catch (error) {
//         console.error("게시글 조회 실패", error);
//         alert("게시글을 불러오는 데 실패했습니다.");
//         navigate("/board");
//       } finally {
//         setIsLoading(false);
//       }
//     };

//     fetchPostAndRecordView();
//   }, [postId, isNewPost, navigate, user]);

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

//     const filesKey = isNewPost ? "attachments" : "newAttachments";
//     newFiles.forEach((file) => formData.append(filesKey, file));

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

//   // 수정 가능 여부를 변수로 관리 (가독성 향상)
//   const canEdit = isNewPost || isAuthor;

//   if (isLoading) {
//     return <div className="p-8">게시글을 불러오는 중입니다...</div>;
//   }

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
//             readOnly={!canEdit}
//           />
//         </div>
//         <div>
//           <label className="label">내용</label>
//           <textarea
//             value={content}
//             onChange={(e) => setContent(e.target.value)}
//             className="textarea textarea-bordered w-full h-40"
//             required
//             readOnly={!canEdit}
//           />
//         </div>

//         {/* 기존 파일 관리 로직을 AttachmentBox 컴포넌트로 대체 */}
//         <AttachmentBox
//           existingAttachments={attachments}
//           onNewFilesChange={setNewFiles} // 자식이 호출할 상태 업데이트 함수 전달
//           onDeletedUrlsChange={setDeletedUrls} // 자식이 호출할 상태 업데이트 함수 전달
//           canEdit={canEdit}
//         />

//         <div className="flex gap-2">
//           {/* '수정 완료' 또는 '작성 완료' 버튼 조건부 렌더링 */}
//           {canEdit && (
//             <button type="submit" className="btn btn-primary">
//               {isNewPost ? "작성 완료" : "수정 완료"}
//             </button>
//           )}
//           {/* 작성자이면서 새 글이 아닐 때만 '삭제' 버튼 표시 */}
//           {isAuthor && !isNewPost && (
//             <button
//               type="button"
//               onClick={handleDeletePost}
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

import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getPost,
  deletePost,
  createPost,
  updatePost,
  recordView,
  toggleLike,
  getLikeStatus,
} from "../api/board";
import CommentsBox from "../components/CommentsBox";
import PostViewer from "../components/PostViewer";
import PostEditor from "../components/PostEditor";
import { useAuth } from "../contexts/AuthContext";

export default function PostDetailPage() {
  const { postId } = useParams();
  const isNewPost = postId === "new";
  const navigate = useNavigate();
  const { user } = useAuth();

  const [isEditing, setIsEditing] = useState(isNewPost);
  const [post, setPost] = useState({
    title: "",
    content: "",
    attachmentUrls: [],
    authorId: null,
    viewCount: 0,
    commentCount: 0,
    likeCount: 0,
  });
  const [isAuthor, setIsAuthor] = useState(false);
  const [isLoading, setIsLoading] = useState(!isNewPost);

  const [liked, setLiked] = useState(false);

  const initiatedFetchPostId = useRef(null);

     // Fetch Post and Record View
    useEffect(() => {
      if (isNewPost) return;
      if (initiatedFetchPostId.current === postId) return;
      initiatedFetchPostId.current = postId;
      const fetchPostAndRecordView = async () => {
        // ... (기존 getPost, recordView 로직)
        try {
          const response = await getPost(postId);
          const postData = response.data;
          setPost(postData);
          if (user && postData.authorId === user.id) {
            setIsAuthor(true);
          } else {
            setIsAuthor(false);
          }
          recordView(postId).catch((err) =>
            console.warn("조회수 기록 실패", err)
          );
          if (user) {
            try {
              const likeResponse = await getLikeStatus(postId);
              setLiked(likeResponse.data.liked);
              // postData에 likeCount가 포함되어 있으므로 setPost를 여기서 다시 할 필요는 없음
            } catch {
              setLiked(false);
            }
          } else {
            setLiked(false);
          }
        } catch (error) {
          console.error("게시글 조회 실패", error);
          alert("게시글을 불러오는 데 실패했습니다.");
          navigate("/board");
        } finally {
          setIsLoading(false);
        }
      };
      fetchPostAndRecordView();

      // **Polling: Like status periodic fetch** 이 블록을 제거하거나 주석 처리
      /*
      let pollingInterval = null;
      if (user) {
        const fetchLikeStatusPeriodically = async () => {
          try {
            const likeResponse = await getLikeStatus(postId);
            setLiked(likeResponse.data.liked);
            setPost(prevPost => ({
              ...prevPost,
              likeCount: likeResponse.data.likeCount,
            }));
          } catch {
            // Fail silently or log to console
          }
        };
        fetchLikeStatusPeriodically(); // Initial fetch
        pollingInterval = setInterval(fetchLikeStatusPeriodically, 5000); // Poll every 5 seconds
      }
      return () => {
        if (pollingInterval) clearInterval(pollingInterval); // Cleanup on unmount
      };
      */
      
      // 폴링이 제거되었으므로, 클린업 로직도 필요 없습니다.
      return () => {}; // 훅이 무언가를 리턴해야 한다면 빈 클린업 함수를 리턴
    }, [postId, isNewPost, navigate, user]);

  // Save post (create/update)
  const handleSave = async (editedPost, newFiles, deletedUrls) => {
    const formData = new FormData();
    formData.append("title", editedPost.title);
    formData.append("content", editedPost.content);

    try {
      if (isNewPost) {
        newFiles.forEach((file) => formData.append("attachments", file));
        await createPost(formData);
        navigate("/board");
      } else {
        newFiles.forEach((file) => formData.append("newAttachments", file));
        deletedUrls.forEach((url) =>
          formData.append("deletedAttachmentUrls", url)
        );
        const response = await updatePost(postId, formData);
        setPost(response.data);
        setIsEditing(false);
      }
    } catch (error) {
      console.error("게시글 저장 실패", error);
      alert("게시글 저장에 실패했습니다.");
    }
  };

  // Delete post
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

  // Cancel editing
  const handleCancel = () => {
    if (isNewPost) {
      navigate("/board");
    } else {
      setIsEditing(false);
    }
  };

  // Like toggle
  const handleToggleLike = async () => {
    if (!user) {
      alert("로그인이 필요합니다.");
      return;
    }

    try {
      await toggleLike(postId);

      const newLiked = !liked;
      setLiked(newLiked);

      setPost((prevPost) => ({
        ...prevPost,
        likeCount: newLiked
          ? prevPost.likeCount + 1
          : prevPost.likeCount - 1,
      }));
    } catch (error) {
      console.error("좋아요 토글 실패", error);
      alert("좋아요 처리에 실패했습니다.");
    }
  };

  if (isLoading) {
    return <div className="p-8">게시글을 불러오는 중입니다...</div>;
  }

  return (
    <div className="p-8 max-w-4xl mx-auto">
      {isEditing ? (
        <PostEditor
          initialPost={{
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
          onEdit={() => setIsEditing(true)}
          onDelete={handleDelete}
          liked={liked}
          likeCount={post.likeCount}
          onToggleLike={handleToggleLike}
        />
      )}

      {!isNewPost && <CommentsBox postId={postId} />}
    </div>
  );
}

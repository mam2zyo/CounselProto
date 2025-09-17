// src/pages/BoardPage.jsx
import { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import { getAllPosts, createPost, deletePost } from "../api/board";

export default function BoardPage() {
  // 변경: accessToken 대신 isLoggedIn 상태를 가져옵니다.
  const { isLoggedIn } = useAuth();
  const [posts, setPosts] = useState([]);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  // 게시글 조회
  // 변경: accessToken 대신 isLoggedIn을 의존성 배열에 추가합니다.
  useEffect(() => {
    // 로그인 상태일 때만 게시글을 가져옵니다.
    if (isLoggedIn) {
      fetchPosts();
    } else {
      // 로그아웃 상태가 되면 게시글 목록을 비웁니다.
      setPosts([]);
    }
  }, [isLoggedIn]);

  const fetchPosts = async () => {
    // isLoggedIn으로 확인하는 것이 더 명확하지만, AuthContext에서 이미 
    // 로그인 상태를 보장하므로 이 조건문은 생략해도 괜찮습니다.
    try {
      // 변경: accessToken을 인자로 전달하지 않습니다.
      const response = await getAllPosts();
      // 안전하게 배열인지 확인
      if (Array.isArray(response.data)) {
        setPosts(response.data);
      } else {
        setPosts([]);
      }
    } catch (error) {
      console.error("게시글 조회 실패", error);
      setPosts([]); // 실패 시 빈 배열 처리
    }
  };

  // 게시글 작성
  const handleCreatePost = async () => {
    if (!title || !content) return;

    const postData = { title, content, attachmentUrls: [] };

    try {
      // 변경: accessToken을 인자로 전달하지 않습니다.
      await createPost(postData);
      setTitle("");
      setContent("");
      fetchPosts(); // 작성 후 목록 갱신
    } catch (error) {
      console.error("게시글 생성 실패", error);
    }
  };

  // 게시글 삭제
  const handleDeletePost = async (id) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;

    try {
      // 변경: accessToken을 인자로 전달하지 않습니다.
      await deletePost(id);
      fetchPosts(); // 삭제 후 목록 갱신
    } catch (error) {
      console.error("게시글 삭제 실패", error);
    }
  };

  // 로그인 안 된 경우 안내
  // 변경: accessToken 대신 isLoggedIn으로 확인합니다.
  if (!isLoggedIn) {
    return (
      <div className="p-8">
        <p>로그인 후 게시판을 이용할 수 있습니다.</p>
      </div>
    );
  }

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold mb-6">게시판</h1>

      {/* 게시글 작성 폼 */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="제목"
          className="input input-bordered w-full mb-2"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          placeholder="내용"
          className="textarea textarea-bordered w-full mb-2"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button className="btn btn-primary" onClick={handleCreatePost}>
          작성
        </button>
      </div>

      {/* 게시글 리스트 */}
      <div className="space-y-4">
        {Array.isArray(posts) && posts.length > 0 ? (
          posts.map((post) => (
            <div
              key={post.postId}
              className="border rounded p-4 shadow hover:bg-base-200 transition"
            >
              <div className="flex justify-between items-start">
                <div>
                  <h2 className="text-xl font-semibold">{post.title}</h2>
                  <p className="text-gray-600">{post.content}</p>
                </div>
                {/* 
                  본인 글만 삭제할 수 있도록 로직을 추가하면 더 좋습니다. 
                  (예: post.authorId === currentUser.id)
                */}
                <button
                  className="btn btn-sm btn-error"
                  onClick={(e) => {
                    e.stopPropagation(); // 부모 div의 클릭 이벤트 방지
                    handleDeletePost(post.postId);
                  }}
                >
                  삭제
                </button>
              </div>
            </div>
          ))
        ) : (
          <p>게시글이 없습니다.</p>
        )}
      </div>
    </div>
  );
}

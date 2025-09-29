// src/api/board.js
import apiClient from "./index";

// 모든 게시글 조회 (검색/정렬/페이지 파라미터 지원 + 정렬 방향까지 서버에 전달)
export const getAllPosts = async ({
  keyword = "",
  sortBy = "latest", // "latest" | "views" | "comments"
  direction = "desc", // "asc" | "desc"  ← 중요!
  page = 0,
  size = 10,
} = {}) => {
  return await apiClient.get("/board", {
    params: { search: keyword, sortBy, direction, page, size },
  });
};

// 특정 게시글 조회 (조회수 증가 없음)
export const getPost = async (id) => {
  return await apiClient.get(`/board/${id}`);
};

// 유니크 조회수 1회 기록 (백엔드: POST /api/board/{id}/view)
export const recordView = async (id) => {
  return await apiClient.post(`/board/${id}/view`);
};

// 게시글 생성 (파일 첨부 때문에 FormData 사용)
export const createPost = async (formData) => {
  return await apiClient.post("/board", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 게시글 수정 (FormData 사용)
export const updatePost = async (id, formData) => {
  return await apiClient.put(`/board/${id}`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 게시글 삭제
export const deletePost = async (id) => {
  return await apiClient.delete(`/board/${id}`);
};

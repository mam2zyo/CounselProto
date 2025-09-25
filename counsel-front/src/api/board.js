// src/api/board.js
import apiClient from "./index";

// 모든 게시글 조회
export const getAllPosts = async () => {
  return await apiClient.get("/board");
};

// 특정 게시글 조회 (백엔드에서 조회수 +1)
export const getPost = async (id) => {
  return await apiClient.get(`/board/${id}`);
};

// 게시글 생성 (파일 첨부 때문에 FormData 사용) - 키: attachments
export const createPost = async (formData) => {
  return await apiClient.post("/board", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 게시글 수정 (FormData 사용) - 키: newAttachments / deletedAttachmentUrls
export const updatePost = async (id, formData) => {
  return await apiClient.put(`/board/${id}`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 게시글 삭제
export const deletePost = async (id) => {
  return await apiClient.delete(`/board/${id}`);
};

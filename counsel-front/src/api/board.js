// src/api/board.js
import apiClient from "./index";

// 모든 게시글 조회
export const getAllPosts = async () => {
  return await apiClient.get("/board");
};

// 특정 게시글 조회 (조회수 증가 없음)
export const getPost = async (id) => {
  return await apiClient.get(`/board/${id}`);
};

// ✅ 유니크 조회수 1회 기록 (백엔드: POST /api/board/{id}/view)
export const recordView = async (id) => {
  return await apiClient.post(`/board/${id}/view`);
};

// 게시글 생성 (파일 첨부 때문에 FormData 사용)
export const createPost = async (formData) => {
  return await apiClient.post("/board", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 게시글 수정 (FormData 사용)
export const updatePost = async (id, formData) => {
  return await apiClient.put(`/board/${id}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 게시글 삭제
export const deletePost = async (id) => {
  return await apiClient.delete(`/board/${id}`);
};

// ----- 과거 axiosInstance 버전 (보관용) -----
// import axiosInstance from "./axiosInstance";
// export const getAllPosts = () => axiosInstance.get("/board");
// export const getPost = (id) => axiosInstance.get(`/board/${id}`);
// export const createPost = (formData) => axiosInstance.post("/board", formData, { headers: { "Content-Type": "multipart/form-data" }});
// export const updatePost = (id, formData) => axiosInstance.put(`/board/${id}`, formData, { headers: { "Content-Type": "multipart/form-data" }});
// export const deletePost = (id) => axiosInstance.delete(`/board/${id}`);

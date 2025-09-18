// src/api/board.js
import axiosInstance from "./axiosInstance";

// 모든 게시글 조회
export const getAllPosts = () => {
  return axiosInstance.get("/board");
};

// 특정 게시글 조회
export const getPost = (id) => {
  return axiosInstance.get(`/board/${id}`);
};

// 게시글 생성 (파일 첨부 때문에 FormData 사용)
export const createPost = (formData) => {
  return axiosInstance.post("/board", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 게시글 수정 (FormData 사용)
export const updatePost = (id, formData) => {
  return axiosInstance.put(`/board/${id}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 게시글 삭제
export const deletePost = (id) => {
  return axiosInstance.delete(`/board/${id}`);
};
// src/api/board.js
import apiClient from "./index";

// 모든 게시글 조회
export const getAllPosts = async () => {
  return await apiClient.get("/board");
};

// 특정 게시글 조회
export const getPost = async (id) => {
  return await apiClient.get(`/board/${id}`);
};

// 게시글 생성 (FormData 전송: 헤더 지정하지 말 것!)
export const createPost = async (formData) => {
  // 전송 전에 실제 키/값 확인(디버그용)
  // for (const [k, v] of formData.entries()) console.log("FD:", k, v);
  return await apiClient.post("/board", formData);
};

// 게시글 수정 (FormData 전송: 헤더 지정하지 말 것!)
export const updatePost = async (id, formData) => {
  // for (const [k, v] of formData.entries()) console.log("FD:", k, v);
  return await apiClient.put(`/board/${id}`, formData);
};

// 게시글 삭제
export const deletePost = async (id) => {
  return await apiClient.delete(`/board/${id}`);
};

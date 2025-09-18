// src/api/comment.js
import axiosInstance from "./axiosInstance";

// 특정 게시글의 모든 댓글 조회
export const getCommentsByPostId = (postId) => {
  return axiosInstance.get(`/board/${postId}/comments`);
};

// 댓글 생성
export const createComment = (postId, commentData) => {
  return axiosInstance.post(`/board/${postId}/comments`, commentData);
};

// 댓글 수정
export const updateComment = (commentId, commentData) => {
  return axiosInstance.put(`/board/comments/${commentId}`, commentData);
};

// 댓글 삭제
export const deleteComment = (commentId) => {
  return axiosInstance.delete(`/board/comments/${commentId}`);
};
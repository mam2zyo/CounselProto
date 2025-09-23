// src/api/user.js
import apiClient from "./index";

// 특정 유저 정보 조회
export const getUser = async (id) => {
  // baseURL이 /api로 설정되어 있으므로 전체 URL을 적을 필요가 없습니다.
  return await apiClient.get(`/users/${id}`);
};

// 유저 삭제
export const deleteUser = async (id) => {
  return await apiClient.delete(`/users/${id}`);
};

// 유저 프로필 조회
export const getUserProfile = async (userId) => {
  return await apiClient.get(`/users/${userId}/profile`);
};

// 유저 프로필 수정
export const updateUserProfile = async (userId, profileData) => {
  // 이전 답변에서 설명한 바와 같이, PUT 요청 시 데이터를 요청 본문(body)으로 보냅니다.
  return await apiClient.put(`/users/${userId}/profile`, profileData);
};

// import axios from "axios";

// const API_BASE = "http://localhost:8080/api/users";

// export const getUser = async (id) => {
//   const response = await axios.get(`${API_BASE}/${id}`);
//   return response.data;
// };

// export const deleteUser = async (id) => {
//   const response = await axios.delete(`${API_BASE}/${id}`);
//   return response.data;
// };

// export const getUserProfile = async (userId) => {
//   const response = await axios.get(`${API_BASE}/${userId}/profile`);
//   return response.data;
// };

// export const updateUserProfile = async (userId, profileData) => {
//   const params = new URLSearchParams(profileData).toString();
//   const response = await axios.put(`${API_BASE}/${userId}/profile?${params}`);
//   return response.data;
// };

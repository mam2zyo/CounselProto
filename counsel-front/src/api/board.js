// src/api/board.js
import axios from "axios";

const apiClient = axios.create({
    baseURL: '/api/conversations',
    withCredentials: true
});

export const getAllPosts = (token) => {
  setAuthHeader(token);
  return apiClient.get("/");
};

export const getPost = (id, token) => {
  setAuthHeader(token);
  return apiClient.get(`/${id}`);
};

export const createPost = (postData, token) => {
  setAuthHeader(token);
  return apiClient.post("/", postData);
};

export const updatePost = (id, postData, token) => {
  setAuthHeader(token);
  return apiClient.put(`/${id}`, postData);
};

export const deletePost = (id, token) => {
  setAuthHeader(token);
  return apiClient.delete(`/${id}`);
};

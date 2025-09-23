// src/api/auth.js
import apiClient from "./index";

export const signup = async (email, password) => {
  return await apiClient.post("/auth/signup", { email, password });
};

export const login = async (email, password) => {
  return await apiClient.post("/auth/login", { email, password });
};

export const logout = async () => {
  return await apiClient.post("/auth/logout");
};

export const refresh = async () => {
  return await apiClient.post("/auth/refresh");
};

// import axios from 'axios';

// const apiClient = axios.create({
//   baseURL: '/api/auth',
//   withCredentials: true
// });

// export const signup = (email, password) => {
//   return apiClient.post("/signup", { email, password });
// };

// export const login = (email, password) => {
//   return apiClient.post("/login", { email, password });
// };

// export const logout = () => {
//   return apiClient.post('/logout');
// };

// export const refresh = () => {
//   return apiClient.post('/refresh');
// }

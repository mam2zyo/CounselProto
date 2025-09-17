// src/api/auth.js
import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api/auth', 
  withCredentials: true
});

export const signup = (email, password) => {
  return apiClient.post("/signup", { email, password });
};

export const login = (email, password) => {
  return apiClient.post("/login", { email, password });
};

export const logout = () => {
  return apiClient.post('/logout');
};

export const refresh = () => {
  return apiClient.post('/refresh');
}
import axios from "axios";

const API_BASE = "http://localhost:8080/api/users";

export const getUser = async (id) => {
  const response = await axios.get(`${API_BASE}/${id}`);
  return response.data;
};

export const deleteUser = async (id) => {
  const response = await axios.delete(`${API_BASE}/${id}`);
  return response.data;
};

export const getUserProfile = async (userId) => {
  const response = await axios.get(`${API_BASE}/${userId}/profile`);
  return response.data;
};

export const updateUserProfile = async (userId, profileData) => {
  const params = new URLSearchParams(profileData).toString();
  const response = await axios.put(`${API_BASE}/${userId}/profile?${params}`);
  return response.data;
};

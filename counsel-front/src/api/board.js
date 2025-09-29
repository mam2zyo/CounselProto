import apiClient from "./index";

export const getAllPosts = async ({
  search = "",
  sortBy = "latest",
  direction = "desc",
  page = 0,
  size = 10,
} = {}) => {
  return await apiClient.get("/board", {
    params: { search, sortBy, direction, page, size },
  });
};

export const getPost = async (id) => {
  return await apiClient.get(`/board/${id}`);
};

export const recordView = async (id) => {
  return await apiClient.post(`/board/${id}/view`);
};

export const createPost = async (formData) => {
  return await apiClient.post("/board", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const updatePost = async (id, formData) => {
  return await apiClient.put(`/board/${id}`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const deletePost = async (id) => {
  return await apiClient.delete(`/board/${id}`);
};

export const toggleLike = async (id) => {
  return await apiClient.post(`/board/${id}/like`);
};

export const getLikeStatus = async (id) => {
  return await apiClient.get(`/board/${id}/like`);
};

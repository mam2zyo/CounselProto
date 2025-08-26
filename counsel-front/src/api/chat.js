// src/api/chat.js
import axios from "axios";

const apiClient = axios.create({
    baseURL: '/api'
});

apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
})

export const sendMessage = (userMessage) => {
    return apiClient.post('/chat', {message: userMessage});
}
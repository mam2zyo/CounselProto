// src/api/conversation.js
import axios from "axios";

const apiClient = axios.create({
    baseURL: '/api/conversations',
    withCredentials: true
});

// 모든 대화 목록 (GET /api/conversations)
export const fetchConversations = () => {
    return apiClient.get();
}

// 특정 대화 상세 (GET /api/conversations/{id})
export const fetchConversationDetail = (id) => {
    return apiClient.get(`/${id}`);
}

// 빈 대화 생성 (POST /api/conversations)
// export const createConversation = () => {
//     return apiClient.post();
// }

// 대화 수정 (PUT /api/conversations/{id})
export const updateConversation = (id, data) => {
    return apiClient.put(`/${id}`, {title: data.title, summary: data.summary});
}

// 대화 삭제 (DELETE /api/conversations/{id})
export const deleteConversation = (id) => {
    return apiClient.delete(`/${id}`);
}

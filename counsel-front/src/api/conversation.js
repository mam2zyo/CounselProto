// src/api/conversation.js
import axios from "axios";

// import { fetchEventSource } from '@microsoft/fetch-event-source';
// axios 가 eventsource 를 지원하지 않아 별개로  url 지정
// const BASE_URL = '/api/conversations';

const apiClient = axios.create({
    baseURL: '/api/conversations'
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


// 모든 대화 목록 (GET /api/conversations)
export const fetchConversations = () => {
    return apiClient.get();
}

// 특정 대화 상세 (GET /api/conversations/{id})
export const fetchConversationDetail = (id) => {
    return apiClient.get(`/${id}`);
}

// 빈 대화 생성 (POST /api/conversations)
export const createConversation = () => {
    return apiClient.post();
}

// 대화 수정 (PUT /api/conversations/{id})
export const updateConversation = (id, data) => {
    return apiClient.put(`/${id}`, {title: data.title, summary: data.summary});
}

// 대화 삭제 (DELETE /api/conversations/{id})
export const deleteConversation = (id) => {
    return apiClient.delete(`/${id}`);
}


export const streamChat = async (conversationId, userMessage, onMessage, onError, onComplete) => {
    try {
        // 토큰을 가져와서 헤더에 설정
        const token = localStorage.getItem('accessToken');
        
        const response = await fetch('/api/conversations/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : '',
            },
            body: JSON.stringify({
                conversationId: conversationId,
                message: userMessage
            }),
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');

        try {
            while (true) {
                const { value, done } = await reader.read();
                if (done) break;
                
                const chunk = decoder.decode(value, { stream: true });
                
                // 스트리밍 데이터를 onMessage 콜백으로 전달
                if (onMessage && chunk) {
                    onMessage(chunk);
                }
            }
            
            // 스트리밍 완료 시 onComplete 호출
            if (onComplete) {
                onComplete();
            }
        } catch (streamError) {
            console.error('Stream reading error:', streamError);
            if (onError) {
                onError(streamError);
            }
        } finally {
            reader.releaseLock();
        }
    } catch (error) {
        console.error('Fetch error:', error);
        if (onError) {
            onError(error);
        }
    }
};
// src/api/index.js
import axios from "axios";

const apiClient = axios.create({
  baseURL: "/api",
  withCredentials: true,
  headers: { "Content-Type": "application/json" }, // 기본 JSON
});

// Spring Security CSRF를 사용하는 경우(백엔드 설정과 이름 일치시켜야 함)
apiClient.defaults.xsrfCookieName = "XSRF-TOKEN";
apiClient.defaults.xsrfHeaderName = "X-XSRF-TOKEN";

export default apiClient;

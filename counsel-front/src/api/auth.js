// src/api/auth.js
import apiClient from "./index";

/**
 * 회원가입
 * 서버 응답이 ResponseMessage 형태면 data를 반환,
 * 아니면 원본 data를 그대로 반환합니다.
 */
export const signup = async (email, password) => {
  const { data } = await apiClient.post("/auth/signup", { email, password });
  return data?.data ?? data;
};

/**
 * 로그인
 */
export const login = async (email, password) => {
  const { data } = await apiClient.post("/auth/login", { email, password });
  return data?.data ?? data;
};

/**
 * 로그아웃
 */
export const logout = async () => {
  const { data } = await apiClient.post("/auth/logout");
  return data?.data ?? data;
};

/**
 * 토큰 리프레시
 */
export const refresh = async () => {
  const { data } = await apiClient.post("/auth/refresh");
  return data?.data ?? data;
};

/**
 * 현재 로그인 유저 조회
 * - 백엔드가 { email }을 직접 주는 경우: 그대로 반환
 * - ResponseMessage 래핑({ code, message, data: { email } })인 경우: data.data 반환
 */
export const me = async () => {
  const { data } = await apiClient.get("/auth/me");
  return data?.data ?? data; // => { email: string }
};

/**
 * 편의 함수: 현재 사용자 이메일만 가져오기
 */
export const getMyEmail = async () => {
  const meData = await me();
  return meData?.email ?? null;
};

import { createContext, useState, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { refresh as refreshApi, me as meApi, logout as logoutApi } from "../api/auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);        // { email } 형태
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        // 1) 리프레시 시도: 쿠키 없으면 401이 떨어짐 (정상 비로그인 흐름)
        try {
          await refreshApi();
        } catch (e) {
          if (e?.response?.status !== 401) {
            console.error("[Auth] refresh error:", e?.response?.status, e?.response?.data || e);
          }
          // 401은 비로그인으로 간주 → 아래 me() 스킵
          setIsLoggedIn(false);
          setUser(null);
          return;
        }

        // 2) me()로 사용자 정보 조회
        const meData = await meApi(); // { email } 또는 래핑된 data
        if (meData?.email) {
          setUser({ email: meData.email });
          setIsLoggedIn(true);
        } else {
          setUser(null);
          setIsLoggedIn(false);
        }
      } catch (error) {
        console.error("[Auth] init auth failed:", error);
        setUser(null);
        setIsLoggedIn(false);
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  // 이메일/비번 로그인 혹은 OAuth 성공 직후, meData를 넘겨 호출
  const login = (meData) => {
    if (meData && meData.email) {
      setUser({ email: meData.email });
      setIsLoggedIn(true);
    } else {
      setUser(null);
      setIsLoggedIn(false);
    }
  };

  const logout = async () => {
    try {
      await logoutApi();
    } catch (e) {
      console.warn("[Auth] logout error (ignored):", e?.response?.status);
    } finally {
      setUser(null);
      setIsLoggedIn(false);
      setIsLoading(false);
      navigate("/login");
    }
  };

  const value = { user, isLoggedIn, isLoading, login, logout };

  if (isLoading) return <div>Loading...</div>;

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
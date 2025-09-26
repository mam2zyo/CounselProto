import { createContext, useState, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { refresh as refreshApi, logout as logoutApi } from "../api/auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true); // ✅ 초기값 true
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        await refreshApi(); // ✅ 쿠키에 있는 리프레시 토큰으로 검증
        setIsLoggedIn(true);
      } catch (error) {
        setIsLoggedIn(false);
        console.log("자동 로그인 실패");
      } finally {
        setIsLoading(false);
      }
    };
    checkAuthStatus();
  }, []);

  const login = () => {
    setIsLoggedIn(true);
    navigate("/");
  };

  const logout = async () => {
    try {
      await logoutApi();
    } catch (error) {
      console.log("로그 아웃 실패", error);
    } finally {
      setIsLoggedIn(false);   // ✅ 상태 초기화 확실히
      setIsLoading(false);
      navigate("/login");
    }
  };

  const value = { isLoggedIn, login, logout, isLoading };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

// src/contexts/AuthContext.jsx
import { createContext, useState, useContext, useEffect } from "react";
import { checkAuthStatus, logout as logoutApi } from "../api/auth";

// user 상태를 관리하도록 변경
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // isLoggedIn -> user 상태로 변경. 초기값은 null.
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // 앱이 처음 로드될 때 쿠키를 통해 사용자 정보를 가져옴
  useEffect(() => {
    const fetchUser = async () => {
      try {
        // 새로 만든 /auth/me API 호출
        const response = await checkAuthStatus();
        // API 응답 데이터(사용자 객체)를 user 상태에 저장
        setUser(response.data.data); // 서버 응답 구조에 따라 .data.data 또는 .data 일 수 있음
      } catch (error) {
        console.log("사용자 정보를 가져오는데 실패했습니다.", error);
        // 실패 시 user를 null로 유지
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchUser();
  }, []);

  // LoginPage에서 호출될 login 함수.
  // 로그인 성공 후 받은 사용자 데이터를 인자로 받아 상태를 업데이트합니다.
  const login = (userData) => {
    setUser(userData);
  };

  const logout = async () => {
    try {
      await logoutApi();
    } catch (error) {
      console.log("로그 아웃 실패", error);
    } finally {
      // 상태를 null로 초기화
      setUser(null);
    }
  };

  // value에 user 객체와 isLoggedIn 불리언을 함께 제공
  const value = {
    user,
    login,
    logout,
    isLoading,
    isLoggedIn: !!user, // user 객체가 있으면 true, null이면 false
  };

  // 초기 로딩 중에는 로딩 화면을 표시
  if (isLoading) {
    return <div>Loading user...</div>;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within a AuthProvider");
  }
  return context;
}

// import { createContext, useState, useContext, useEffect } from "react";
// import { useNavigate } from "react-router-dom";
// import { refresh as refreshApi, logout as logoutApi } from "../api/auth";

// const AuthContext = createContext(null);

// export function AuthProvider({ children }) {
//   const [isLoggedIn, setIsLoggedIn] = useState(false);
//   const [isLoading, setIsLoading] = useState(true); // ✅ 초기값 true
//   const navigate = useNavigate();

//   useEffect(() => {
//     const checkAuthStatus = async () => {
//       try {
//         await refreshApi(); // ✅ 쿠키에 있는 리프레시 토큰으로 검증
//         setIsLoggedIn(true);
//       } catch (error) {
//         setIsLoggedIn(false);
//         console.log("자동 로그인 실패");
//       } finally {
//         setIsLoading(false);
//       }
//     };
//     checkAuthStatus();
//   }, []);

//   const login = () => {
//     setIsLoggedIn(true);
//     navigate("/");
//   };

//   const logout = async () => {
//     try {
//       await logoutApi();
//     } catch (error) {
//       console.log("로그 아웃 실패", error);
//     } finally {
//       setIsLoggedIn(false);   // ✅ 상태 초기화 확실히
//       setIsLoading(false);
//       navigate("/login");
//     }
//   };

//   const value = { isLoggedIn, login, logout, isLoading };

//   if (isLoading) {
//     return <div>Loading...</div>;
//   }

//   return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
// }

// export function useAuth() {
//   return useContext(AuthContext);
// }

// src/pages/LoginPage.jsx
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login as loginApi, refresh as refreshApi, me as meApi } from "../api/auth";
import { useAuth } from "../contexts/AuthContext";
import { FcGoogle } from "react-icons/fc";
import { SiNaver } from "react-icons/si";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth(); // ✅ login(meData) 형태

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      // 1) 로그인: 서버가 쿠키에 토큰/세션을 심음 (바디에 accessToken 없음)
      await loginApi(email, password);

      // 2) (선택) access 토큰 보장을 위해 refresh 시도 (실패해도 무시 가능)
      try { await refreshApi(); } catch (_) {}

      // 3) 현재 사용자 정보 조회 (쿠키 기반으로 인증됨)
      const meData = await meApi(); // { email } 또는 { id, email }

      // 4) 컨텍스트 업데이트
      login(meData);

      // 5) 이동
      navigate("/");
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.";
      setError(msg);
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleOAuthLogin = (provider) => {
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900 px-4">
      <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-12">
        {/* 왼쪽 소개 */}
        <div className="flex flex-col items-center justify-center text-center p-10 bg-white dark:bg-gray-800 rounded-2xl shadow-lg">
          <h1 className="text-5xl font-bold mb-4 text-gray-900 dark:text-white">고민 상담</h1>
          <p className="text-lg mb-6 text-gray-700 dark:text-gray-300">당신의 마음에 귀 기울이는 AI 친구</p>
          <img src="/회원가입그림.jpg" alt="회원가입 안내" className="w-80 rounded-lg shadow-md" />
        </div>

        {/* 오른쪽 로그인 */}
        <div className="flex flex-col bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-10">
          <h2 className="text-3xl font-semibold text-center mb-8 text-gray-900 dark:text-white">
            로그인 / 회원가입
          </h2>

          <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
            <input
              type="email"
              placeholder="이메일"
              className="input input-bordered w-full dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={submitting}
            />
            <input
              type="password"
              placeholder="비밀번호"
              className="input input-bordered w-full dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={submitting}
            />
            {error && <p className="text-error text-sm">{error}</p>}
            <button
              type="submit"
              className="btn btn-primary w-full mt-2 dark:bg-blue-600 dark:text-white dark:hover:bg-blue-700"
              disabled={submitting}
            >
              {submitting ? "로그인 중..." : "이메일로 로그인"}
            </button>
          </form>

          <div className="divider my-6 border-gray-300 dark:border-gray-600 text-gray-500 dark:text-gray-400">또는</div>

          <div className="flex flex-col gap-3">
            <button
              onClick={() => handleOAuthLogin("google")}
              className="btn btn-outline flex items-center justify-center gap-3 w-full border-gray-300 text-gray-900 hover:bg-gray-50 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700"
              disabled={submitting}
            >
              <FcGoogle size={24} />
              구글로 로그인
            </button>
            <button
              onClick={() => handleOAuthLogin("naver")}
              className="btn flex items-center justify-center gap-3 w-full bg-[#03C75A] text-white hover:bg-[#03C75A]/90 dark:bg-[#03C75A] dark:hover:bg-[#03C75A]/80"
              disabled={submitting}
            >
              <SiNaver size={24} />
              네이버로 로그인
            </button>
          </div>

          <div className="text-center mt-6">
            <Link to="/signup" className="link link-hover text-gray-700 dark:text-gray-300">
              아직 회원이 아니신가요? 회원가입
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;

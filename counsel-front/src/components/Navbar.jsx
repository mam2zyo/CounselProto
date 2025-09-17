import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { useEffect, useState } from "react";

function Navbar() {
  const { isLoggedIn, logout } = useAuth();
  const navigate = useNavigate();

  const themes = ["light", "dark", "cupcake", "retro", "luxury", "cyberpunk"];
  const [theme, setTheme] = useState(localStorage.getItem("theme") || themes[0]);

  // HTML data-theme 적용 + localStorage 저장
  useEffect(() => {
    const html = document.querySelector("html");
    if (html) {
      html.setAttribute("data-theme", theme);
      localStorage.setItem("theme", theme);
    }
  }, [theme]);

  // 테마 순환
  const cycleTheme = () => {
    const currentIndex = themes.indexOf(theme);
    const nextIndex = (currentIndex + 1) % themes.length;
    setTheme(themes[nextIndex]);
  };

  return (
    <div className="navbar bg-base-100 shadow-md relative">
      {/* 좌측 메뉴 버튼 (모바일용) */}
      <div className="navbar-start">
        <label htmlFor="my-drawer" className="btn btn-square btn-ghost lg:hidden">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            className="inline-block w-5 h-5 stroke-current"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16"></path>
          </svg>
        </label>
      </div>

      {/* 게시판 버튼: 중앙보다 왼쪽 */}
      <div className="absolute left-1/4 transform -translate-x-1/2">
        <button
          className="btn btn-primary btn-sm px-5 py-4 text-lg"
          onClick={() => {
            if (isLoggedIn) navigate("/board");
            else {
              alert("로그인 후 이용 가능합니다.");
              navigate("/login");
            }
          }}
        >
          게시판
        </button>
      </div>

      {/* 고민 상담 버튼: 중앙 고정 */}
      <div className="absolute left-1/2 transform -translate-x-1/2">
        <button
          className="
            text-3xl font-bold px-6 py-2
            border-2 border-current rounded-xl
            bg-base-100 text-base-content shadow-lg
            hover:bg-base-200 transition-colors duration-200
            cursor-pointer
          "
          style={{ lineHeight: 1 }}
          onClick={() => {}}
        >
          고민 상담
        </button>
      </div>

      {/* 오른쪽 버튼들: 마이 페이지, 로그인/로그아웃, 테마 */}
      <div className="navbar-end gap-2 flex items-center">
        {/* 마이 페이지 드롭다운 */}
        <div className="dropdown dropdown-end">
          <label tabIndex={0} className="btn btn-ghost m-1">
            마이 페이지 ▼
          </label>
          <ul
            tabIndex={0}
            className="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-52"
          >
            <li>
              <Link to="/profile">프로필</Link>
            </li>
            <li>
              <Link to="/change-password">비밀번호 변경</Link>
            </li>
            <li>
              <Link to="/settings">설정</Link>
            </li>
          </ul>
        </div>

        {isLoggedIn ? (
          <button onClick={logout} className="btn btn-ghost">
            로그아웃
          </button>
        ) : (
          <Link to="/login" className="btn btn-ghost">
            로그인
          </Link>
        )}

        {/* 테마 순환 버튼 */}
        <button onClick={cycleTheme} className="btn btn-ghost">
          {theme}
        </button>
      </div>
    </div>
  );
}

export default Navbar;

import { Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { useEffect, useState } from "react";

function Navbar() {
  const { isLoggedIn, logout } = useAuth();

  const themes = [
    "light",
    "dark",
    "cupcake",
    "retro",
    "luxury",
    "cyberpunk",
    
  ];

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
    <div className="navbar bg-base-100">
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

      <div className="navbar-center">
        <a className="btn btn-ghost text-xl">고민 상담</a>
      </div>

      <div className="navbar-end gap-2">
        {isLoggedIn ? (
          <button onClick={logout} className="btn btn-ghost">로그아웃</button>
        ) : (
          <Link to="/login" className="btn btn-ghost">로그인</Link>
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

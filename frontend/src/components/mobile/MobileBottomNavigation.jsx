import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { User, Heart, Search } from "lucide-react";
import logo from "../../assets/Logo.png";
import AccountSidebar from "../Header/AccountSidebar.jsx";

export default function MobileBottomNavigation({ user, onLogout }) {
  const [isAccountSidebarOpen, setIsAccountSidebarOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  // 화면 크기 감지
  useEffect(() => {
    function handleResize() {
      setIsMobile(window.innerWidth <= 640); // 640px 이하 모바일로 판단
    }

    handleResize(); // 초기 실행
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const openAccountSidebar = () => {
    setIsAccountSidebarOpen(true);
  };

  const closeAccountSidebar = () => {
    setIsAccountSidebarOpen(false);
  };

  // 모바일이 아닐 때는 아무것도 렌더하지 않음
  if (!isMobile) return null;

  return (
      <>
        {/* Mobile Bottom Navigation */}
        <nav className="lg:hidden bg-white fixed bottom-0 w-full flex justify-around items-center py-2 shadow z-20">
          <Link to={`/`}>
            <img src={logo} alt="Logo" className="h-8" />
          </Link>

          <button>
            <Link to={`/search`}>
              <Search />
            </Link>
          </button>

          <button>
            <Link to={`/wishes`}>
              <Heart />
            </Link>
          </button>

          {/* 프로필 버튼 - 클릭 시 사이드바 열림 */}
          <button onClick={openAccountSidebar}>
            <User />
          </button>
        </nav>

        {/* AccountSidebar 렌더링 */}
        {isAccountSidebarOpen && (
            <AccountSidebar
                user={user}
                onClose={closeAccountSidebar}
                onLogout={() => {
                  onLogout();
                  closeAccountSidebar();
                }}
            />
        )}
      </>
  );
}

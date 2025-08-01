import React,{ useContext, useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import axios from "axios";
import { CartContext } from "../contexts/CartContext";
import { Menu, MapPin, Search, Heart, ShoppingCart, User, LogOut, ChevronDown, X } from "lucide-react";
import logo from "../assets/Logo.png"

export default function Header({ isCatalogOpen, setIsCatalogOpen }) {
  const location = useLocation();
  const isMobileSearchPage = location.pathname === "/search";

  const { isLoggedIn, user, logout } = useAuth();
  const { cartItemCount } = useContext(CartContext);
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false); // 첫번째 코드에 있던 사이드바 열림 상태 (필요하면 활용)

  // 로그아웃 핸들러
  const handleLogout = () => {
    logout();
    setIsAccountOpen(false);
    setIsSidebarOpen(false);
    navigate('/');
  };

  // 카테고리 데이터 요청
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/categories');
        setCategories(response.data);
      } catch (err) {
        console.error('Error fetching categories:', err);
      }
    };
    fetchCategories();
  }, []);

  // 카테고리 클릭 시 이동
  const handleCategoryClick = (categoryId) => {
    navigate(`/products?categoryId=${categoryId}`);
    setIsCatalogOpen(false);
  };

  // 검색 실행
  const handleSearch = () => {
    if (searchTerm.trim()) {
      navigate(`/products?keyword=${encodeURIComponent(searchTerm.trim())}`);
      setSearchTerm('');
    }
  };

  // 엔터키 검색 실행
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // 사이드바 토글
  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  if (isMobileSearchPage) return null; // <- /search면 렌더링하지 않음

  return (
      <>
        <header className="fixed top-0 left-0 w-full bg-white shadow p-2 flex items-center justify-between z-20" style={{ height: '48px' }}>
          <div className="flex items-center gap-3">
            {/* 로고 */}
            <Link to="/" aria-label="Home">
              <div className="flex items-center gap-3">
                <img
                    src={logo}
                    alt="Company Logo"
                    className="hidden lg:block"
                    style={{ height: '32px' }}
                />
              </div>
            </Link>
            {/* Category */}
            <button
                onClick={() => setIsCatalogOpen(true)}
                className="cursor-pointer"
                aria-label="Open menu"
            >
              <Menu className="w-5 h-5" />
            </button>
            {/* PostalCode (UserOnly) */}
            <button
                className="flex items-center gap-1 text-xs text-gray-700"
                aria-label="Location"
            >
              <MapPin size={14} />
              Tokyo
            </button>
          </div>

          {/* 검색, 위시리스트, 장바구니, 프로필 영역 */}
          <div className="flex items-center gap-3">

            {/* 검색 */}
            <div className="hidden lg:flex items-center border rounded overflow-hidden bg-gray-50" style={{ height: '32px' }}>
              <input
                  type="search"
                  placeholder="探す..."
                  className="px-2 py-0 outline-none bg-gray-50 text-sm"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={handleKeyPress}
                  aria-label="Search products"
                  style={{ height: '32px' }}
              />
              <button
                  className="px-2 bg-purple-600 text-white flex items-center justify-center"
                  onClick={handleSearch}
                  aria-label="Submit search"
                  style={{ height: '32px' }}
              >
                <Search size={14} />
              </button>
            </div>

            {/* 위시리스트 */}
            <Link to={`/wishes`} aria-label="Wishlist">
              <Heart className="hidden lg:flex text-gray-700 w-5 h-5 hover:text-purple-600 cursor-pointer" />
            </Link>

            {/* 장바구니 */}
            <Link to="/cart" className="relative" aria-label="Shopping cart">
              <ShoppingCart className="text-gray-700 w-5 h-5 hover:text-purple-600 cursor-pointer" />
              {cartItemCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 bg-red-500 text-white text-[10px] rounded-full h-4 w-4 flex items-center justify-center">
                {cartItemCount}
              </span>
              )}
            </Link>

            {/* 프로필 영역 */}
            {isLoggedIn ? (
                <button
                    onClick={() => setIsAccountOpen(true)}
                    className="flex items-center gap-1 text-xs font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
                    aria-label="My account"
                >
                  <User className="hidden lg:block w-5 h-5" />
                  <span className="hidden lg:block">{'アカウント'}</span>
                </button>
            ) : (
                <Link
                    to="/login"
                    className="flex items-center gap-1 text-xs font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
                    aria-label="Login"
                >
                  <User className="hidden lg:block w-5 h-5" />
                  <span className="hidden lg:block">Login</span>
                </Link>
            )}
          </div>
        </header>

        {/* 카탈로그 사이드바 */}
        {isCatalogOpen && (
            <div className="fixed inset-0 z-50 overflow-hidden">
              <div
                  className="absolute inset-0 bg-black bg-opacity-50"
                  onClick={() => setIsCatalogOpen(false)}
                  aria-hidden="true"
              ></div>

              <div className="absolute inset-y-0 left-0 w-64 bg-white shadow-lg flex flex-col">
                <div className="p-4 flex justify-between items-center">
                  <button
                      onClick={() => setIsCatalogOpen(false)}
                      className="text-gray-500 hover:text-gray-700"
                      aria-label="Close menu"
                  >
                    <X className="w-6 h-6" />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto">
                  <Link
                      to="/products"
                      className="block px-4 py-3 text-gray-700 hover:bg-gray-100 border-b"
                      onClick={() => setIsCatalogOpen(false)}
                  >
                    すべて
                  </Link>
                  {categories.map((category) => (
                      <button
                          key={category.id}
                          onClick={() => handleCategoryClick(category.id)}
                          className="block w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-100 border-b"
                      >
                        {category.name}
                      </button>
                  ))}
                </div>
              </div>
            </div>
        )}

        {/* 계정 사이드바 */}
        {isAccountOpen && (
            <div className="fixed inset-0 z-50 overflow-hidden">
              <div
                  className="absolute inset-0 bg-black bg-opacity-50"
                  onClick={() => setIsAccountOpen(false)}
              ></div>
              <div className="absolute inset-y-0 right-0 max-w-full flex">
                <div className="relative w-screen max-w-md">
                  <div className="h-full flex flex-col bg-white shadow-xl">
                    <div className="flex items-center justify-between p-4">
                      <button
                          onClick={() => setIsAccountOpen(false)}
                          className="text-gray-500 hover:text-gray-700"
                          aria-label="Close menu"
                      >
                        <X size={24} />
                      </button>
                    </div>
                    <div className="flex-1 overflow-y-auto p-4">
                      <div className="space-y-4">
                        <div className="p-4 border rounded-lg">
                          <h3 className="font-medium">
                            メールアドレス
                          </h3>
                          <p className="text-sm text-gray-600 mt-2">
                            {user?.email || ''}
                          </p>
                        </div>

                        <div className="space-y-2">
                          <Link
                              to="/orders"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={() => setIsAccountOpen(false)}
                          >
                            注文履歴
                          </Link>
                          <Link
                              to="/wishes"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={() => setIsAccountOpen(false)}
                          >
                            お気に入り
                          </Link>
                          <Link
                              to="/profile"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={() => setIsAccountOpen(false)}
                          >
                            マイプロフィール変更
                          </Link>
                        </div>

                        <button
                            onClick={handleLogout}
                            className="w-full p-3 text-left text-red-600 hover:bg-red-50 rounded-lg"
                        >
                          ログアウト
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
        )}
      </>
  );
}
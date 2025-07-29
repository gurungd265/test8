import React,{ useContext, useState } from "react";
import {
  Menu,
  MapPin,
  Search,
  Heart,
  ShoppingCart,
  User,
  LogOut,
  X,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import logo from "../assets/Logo.png"
import { CartContext } from "../contexts/CartContext";

export default function Header() {
  const { isLoggedIn, user, logout } = useAuth();
  const { cartItemCount } = useContext(CartContext);
  const navigate = useNavigate();

  // 사이드바 열림 상태 추가
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    setIsSidebarOpen(false);  // 로그아웃시 사이드바 닫기
    navigate("/");
  };

  // 사이드바 토글 함수
  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  return (
      <>
        {/* Header */}
        <header className="bg-white shadow p-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Menu className="lg:hidden cursor-pointer" />
            {/* Logo */}
            <div className="flex items-center gap-4">
              <Link to="/">
                <img src={logo} alt="Logo" className="hidden lg:block h-8" />
              </Link>
              {/* Location */}
              <button className="flex items-center gap-1 text-sm text-gray-700">
                <MapPin size="16" />
                Tokyo
              </button>
              {/* Catalog */}
              <button className="hidden lg:block text-sm font-semibold">
                Catalog
              </button>
            </div>
          </div>
          <div className="flex items-center gap-4">
            {/* Search Section */}
            <div className="flex items-center border rounded overflow-hidden bg-gray-50">
              <input
                  type="search"
                  placeholder="Search..."
                  className="px-3 py-1 outline-none bg-gray-50"
              />
              <button className="p-3 bg-purple-600 text-white">
                <Search size="16" />
              </button>
            </div>
            {/* Liked Products */}
            <Link to={`/wishes`}>
              <Heart className="text-gray-700 w-6 h-6 hover:text-purple-600 cursor-pointer" />
            </Link>
            {/* Shopping Cart */}
            <div>
              <Link to="/cart" className="relative">
                <ShoppingCart className="text-gray-700 w-6 h-6 hover:text-purple-600 cursor-pointer" />
                {cartItemCount > 0 && (
                    <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                  {cartItemCount}
                </span>
                )}
              </Link>
            </div>
            {/* Profile */}
            {isLoggedIn ? (
                // My Page 버튼을 누르면 사이드바 토글
                <button
                    onClick={toggleSidebar}
                    className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
                >
                  <User className="hidden lg:block" />
                  <span className="hidden lg:block">My Page</span>
                </button>
            ) : (
                <Link
                    to="/login"
                    className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
                >
                  <User className="hidden lg:block" />
                  <span className="hidden lg:block">Login</span>
                </Link>
            )}
          </div>
        </header>

        {/* Sidebar for My Page */}
        {isSidebarOpen && (
            <div className="fixed inset-0 z-50 overflow-hidden">
              <div
                  className="absolute inset-0 bg-black bg-opacity-50"
                  onClick={toggleSidebar}
              ></div>
              <div className="absolute inset-y-0 right-0 max-w-full flex">
                <div className="relative w-screen max-w-md">
                  <div className="h-full flex flex-col bg-white shadow-xl">
                    <div className="flex items-center justify-between p-4 border-b">
                      <h2 className="text-lg font-semibold">My Account</h2>
                      <button
                          onClick={toggleSidebar}
                          className="text-gray-500 hover:text-gray-700"
                      >
                        <X size={24} />
                      </button>
                    </div>
                    <div className="flex-1 overflow-y-auto p-4">
                      <div className="space-y-4">
                        <div className="p-4 border rounded-lg">
                          <h3 className="font-medium">Account Information</h3>
                          <p className="text-sm text-gray-600 mt-2">
                            {user?.email || ""}
                          </p>
                        </div>

                        <div className="space-y-2">
                          <Link
                              to="/orders"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={toggleSidebar}
                          >
                            My Orders
                          </Link>
                          <Link
                              to="/wishlist"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={toggleSidebar}
                          >
                            Wishlist
                          </Link>
                          <Link
                              to="/profile"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={toggleSidebar}
                          >
                            Edit profile
                          </Link>
                        </div>

                        <button
                            onClick={handleLogout}
                            className="w-full p-3 text-left text-red-600 hover:bg-red-50 rounded-lg"
                        >
                          Logout
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
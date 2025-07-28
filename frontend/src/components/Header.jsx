import React from "react";
import {
  Menu,
  MapPin,
  Search,
  Heart,
  ShoppingCart,
  User,
  LogOut,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import logo from "../assets/Logo.png"

export default function Header() {
  const { isLoggedIn, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
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
              <img
                src={logo}
                alt="Logo"
                className="hidden lg:block h-8"
              />
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
          {/* Liked Prdoucts */}
          <Link to={`/wishes`}>
            <Heart className="text-gray-700 w-6 h-6 hover:text-purple-600 cursor-pointer" />
          </Link>
          {/* Shopping Cart */}
          <Link to="/cart" className="relative">
            <ShoppingCart className="text-gray-700 w-6 h-6 hover:text-purple-600 cursor-pointer" />
            {/* item count ++ */}
            {/* <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">3</span> */}
          </Link>
          {/* Profile */}
          {isLoggedIn ? (
            <button
              onClick={handleLogout}
              className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-red-500 cursor-pointer"
            >
              <LogOut className="hidden lg:block" />
              <span className="hidden lg:block">
                {user?.email ? user.email : "Logout"}
              </span>
              {/*                     <User className="hidden lg:block cursor-pointer" /> */}
            </button>
          ) : (
            <Link
              to="/login"
              className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
            >
              <User className="hidden lg:block" />
              <span className="hidden lg:block">Login</span>
              {/*                 <User className="hidden lg:block cursor-pointer" /> */}
            </Link>
          )}
        </div>
      </header>
    </>
  );
}

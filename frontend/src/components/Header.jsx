import React, {useState,useEffect} from 'react';
import {Menu, MapPin, Search, Heart, ShoppingCart, User, LogOut} from 'lucide-react';
import {Link, useNavigate} from 'react-router-dom';

export default function Header() {
    const [isLoggedIn,setIsLoggedIn] = useState(false);
    const navigate = useNavigate();

    useEffect(() =>{
        const token = localStorage.getItem('jwtToken');
        setIsLoggedIn(!!token);
        },[]);

    useEffect(() =>{
        const handleStorageChange = ()=>{
                const token =localStorage.getItem('jwtToken');
                setIsLoggedIn(!!token);
            };
        window.addEventListener('storage',handleStorageChange);
        return ()=>{
            window.removeEventListener('storage',handleStorageChange);
            };
        },[]);
    const handleLogout = () =>{
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('tokenType');
        localStorage.removeItem('userEmail');
        setIsLoggedIn(false);
        navigate('/');
//         window.location.reload();
        };


  return (
    <>
      {/* Header */}
      <header className="bg-white shadow p-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Menu className="lg:hidden cursor-pointer" />
          {/* Logo */}
          <Link to="/">
            <div className="flex items-center gap-4">
                <img
                    src="https://cal.co.jp/wordpress/wp-content/themes/temp_calrenew/img/logo.svg"
                    alt="Logo"
                    className="hidden lg:block h-8"
                />
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
        </Link>
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
          <Heart className="cursor-pointer" />
          {/* Shopping Cart */}
          <ShoppingCart className="hidden lg:block cursor-pointer" />
          {/* Profile */}
            {isLoggedIn ? (
                <button onClick={handleLogout} className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-red-500 cursor-pointer">
                    <LogOut className="hidden lg:block" />
                    <span className="hidden lg:block"> Logout</span>
{/*                     <User className="hidden lg:block cursor-pointer" /> */}
                </button>
            ) : (
              <Link to="/login" className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-blue-500 cursor-pointer">
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

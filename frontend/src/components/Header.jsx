import React,{ useContext, useState } from "react";
import { Menu, MapPin, Search, Heart, ShoppingCart, User, LogOut, X } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import logo from "../assets/Logo.png"
import { CartContext } from "../contexts/CartContext";

export default function Header() {
  const { isLoggedIn, user, logout } = useAuth();
  const { cartItemCount } = useContext(CartContext);
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [isCatalogOpen, setIsCatalogOpen] = useState(false);
  const [isAccountOpen, setIsAccountOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const handleLogout = () => {
    logout();
    setIsSidebarOpen(false);  // 로그아웃시 사이드바 닫기
    // navigate("/");
  };

// Fetch categories on component mount
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

  const handleCategoryClick = (categoryId) => {
    navigate(`/products?categoryId=${categoryId}`);
    setIsCatalogOpen(false);
  };

  const handleSearch = () => {
    if (searchTerm.trim()) {
      navigate(`/products?keyword=${encodeURIComponent(searchTerm.trim())}`);
      setSearchTerm('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
      <>
        {/* Header */}
        <header className="bg-white shadow p-4 flex items-center justify-between relative">
          <div className="flex items-center gap-4">
            {/* Mobile menu button */}
            <button
                onClick={() => setIsCatalogOpen(true)}
                className="lg:hidden cursor-pointer"
                aria-label="Open menu"
            >
              <Menu className="w-6 h-6" />
            </button>
            {/* Logo */}
            <Link to="/" aria-label="Home">
              <div className="flex items-center gap-4">
                <img src={logo} alt="Company Logo" className="hidden lg:block h-8" />
                {/* Location */}
                <button className="flex items-center gap-1 text-sm text-gray-700" aria-label="Location">
                  <MapPin size="16" />
                  Tokyo
                </button>
              </div>
            </Link>
            {/* Catalog Button*/}
            <button
                className="hidden lg:flex items-center gap-1 text-sm font-semibold hover:text-purple-600"
                onClick={() => setIsCatalogOpen(true)}
                aria-label="Browse categories"
            >
              Catalog
              <ChevronDown size="16" />
            </button>
          </div>
          <div className="flex items-center gap-4">
            {/* Search Section */}
            <div className="flex items-center border rounded overflow-hidden bg-gray-50">
              <input
                  type="search"
                  placeholder="Search..."
                  className="px-3 py-1 outline-none bg-gray-50"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={handleKeyPress}
                  aria-label="Search products"
              />
              <button
                  className="p-3 bg-purple-600 text-white"
                  onClick={handleSearch}
                  aria-label="Submit search"
              >
                <Search size="16" />
              </button>
            </div>
            {/* Wishlist */}
            <Link to={`/wishes`} aria-label="Wishlist">
              <Heart
                  className="text-gray-700 w-6 h-6 hover:text-purple-600 cursor-pointer"
              />
            </Link>
            {/* Shopping Cart */}
            <Link to="/cart" className="relative" aria-label="Shopping Cart">
              <ShoppingCart className="text-gray-700 w-6 h-6 hover:text-purple-600 cursor-pointer" />
              {cartItemCount > 0 && (
                  <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                  {cartItemCount}
                </span>
              )}
            </Link>
            {/* Profile */}
            {isLoggedIn ? (
                // My Page 버튼을 누르면 사이드바 토글
                <button
                    onClick={() => setIsAccountOpen(true)}
                    className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
                    aria-label="My account"

                >
                  <User className="hidden lg:block" />
                  <span className="hidden lg:block">{user?.email || 'My Page'}</span>
                </button>
            ) : (
                <Link to="/login"
                      className="flex items-center gap-1 text-sm font-semibold text-gray-700 hover:text-blue-500 cursor-pointer"
                      aria-label="Login"
                >
                  <User className="hidden lg:block" />
                  <span className="hidden lg:block">
                  Login
                </span>
                </Link>
            )}
          </div>
        </header>

        {/* Catalog Sidebar */}
        {isSidebarOpen && (
            <div className="fixed inset-0 z-50 overflow-hidden">
              <div
                  className="absolute inset-0 bg-black bg-opacity-50"
                  onClick={() => setIsCatalogOpen(false)}
                  aria-hidden="true"
              ></div>
              <div
                  className="absolute inset-y-0 left-0 w-64 bg-white shadow-lg flex flex-col"
              >
                <div
                    className="p-4 border-b flex justify-between items-center"
                >
                  <h2 className="text-lg font-semibold">
                    Catalog
                  </h2>
                  <button
                      onClick={() => setIsCatalogOpen(false)}
                      className="text-gray-500 hover:text-gray-700"
                      aria-label="Close menu"
                  >
                    <X className="w-6 h-6" />
                  </button>
                </div>
                <div className="flex-1 overflow-y-auto">
                  <Link to="/products"
                        className="block px-4 py-3 text-gray-700 hover:bg-gray-100 border-b"
                        onClick={() => setIsCatalogOpen(false)}
                  >
                    All Products
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

        {/* Account Sidebar */}
        {isAccountOpen && (
            <div className="fixed inset-0 z-50 overflow-hidden">
              <div
                  className="absolute inset-0 bg-black bg-opacity-50"
                  onClick={() => setIsAccountOpen(false)}
              >
              </div>
              <div className="absolute inset-y-0 right-0 max-w-full flex">
                <div className="relative w-screen max-w-md">
                  <div className="h-full flex flex-col bg-white shadow-xl">
                    <div className="flex items-center justify-between p-4 border-b">
                      <h2 className="text-lg font-semibold">My Account</h2>
                      <button
                          onClick={() => setIsAccountOpen(false)}
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
                              onClick={() => setIsAccountOpen(false)}
                          >
                            My Orders
                          </Link>
                          <Link
                              to="/wishes"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={() => setIsAccountOpen(false)}
                          >
                            Wishlist
                          </Link>
                          <Link
                              to="/profile"
                              className="block p-3 hover:bg-gray-100 rounded-lg"
                              onClick={() => setIsAccountOpen(false)}
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
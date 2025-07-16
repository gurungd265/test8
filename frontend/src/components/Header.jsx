import {Menu, MapPin, Search, Heart, ShoppingCart, User} from 'lucide-react';

export default function Header() {
  return (
    <>
      {/* Header */}
      <header className="bg-white shadow p-4 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Menu className="lg:hidden cursor pointer" />
          {/* Logo */}
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
          <User className="hidden lg:block cursor-pointer" />
        </div>
      </header>
    </>
  );
}

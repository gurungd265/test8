import { Link } from "react-router-dom";
import { Menu, User, ShoppingCart, Heart, Search, MapPin } from "lucide-react";

export default function MobileBottomNavigation() {
  return (
    <>
      {/* Mobile Bottom Navigation */}
      <nav className="lg:hidden bg-white fixed bottom-0 w-full flex justify-around items-center py-2 shadow">
        <Link to={`/`}>
          <img
            src="https://cal.co.jp/wordpress/wp-content/themes/temp_calrenew/img/logo.svg"
            alt="Logo"
            className="h-6"
          />
        </Link>
        <button>
          <Search />
        </button>
        <button>
          <Link to={`/cart`}>
            <ShoppingCart />
          </Link>
        </button>
        <button>
          <Link to={`/profile`}>
            <User />
          </Link>
        </button>
      </nav>
    </>
  );
}

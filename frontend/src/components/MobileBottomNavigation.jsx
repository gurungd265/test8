import { Link } from "react-router-dom";
import { User, ShoppingCart, Search } from "lucide-react";
import logo from "../assets/Logo.png"

export default function MobileBottomNavigation() {
  return (
    <>
      {/* Mobile Bottom Navigation */}
      <nav className="lg:hidden bg-white fixed bottom-0 w-full flex justify-around items-center py-2 shadow">
        <Link to={`/`}>
          <img
            src={logo}
            alt="Logo"
            className="h-8"
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

import { Link } from "react-router-dom";
import { User, Heart, Search, ChevronDown } from "lucide-react";
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
          <Link to={`/search`}>
            <Search />
          </Link>
        </button>
        <button>
          <Link to={`/wishes`}>
            <Heart />
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

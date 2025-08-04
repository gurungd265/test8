import { Link } from "react-router-dom";
import { User, Heart, Search, Menu } from "lucide-react";
import logo from "../assets/Logo.png"

export default function MobileBottomNavigation({ setIsCatalogOpen }) {

  const onOpenCatalog = () => {
    setIsCatalogOpen(true);
  };

  return (
    <>
      {/* Mobile Bottom Navigation */}
      <nav className="lg:hidden bg-white fixed bottom-0 w-full flex justify-around items-center py-2 shadow z-20">

        <Link to={`/`}>
          <img src={logo} alt="Logo" className="h-8"/>
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

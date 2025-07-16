import { Menu, User, ShoppingCart, Heart, Search, MapPin } from "lucide-react";

export default function MobileBottomNavigation() {
  return (
    <>
      {/* Mobile Bottom Navigation */}
      <nav className="lg:hidden bg-white fixed bottom-0 w-full flex justify-around items-center py-2 shadow">
        <img
          src="https://cal.co.jp/wordpress/wp-content/themes/temp_calrenew/img/logo.svg"
          alt="Logo"
          className="h-6"
        />
        <button>
          <Search />
        </button>
        <button>
          <ShoppingCart />
        </button>
        <button>
          <User />
        </button>
      </nav>
    </>
  );
}

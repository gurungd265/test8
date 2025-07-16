import Header from './components/Header';
import Promotions from './components/Promotions';
import Products from './components/Products';
import Footer from './components/Footer';
import MobileBottomNavigation from './components/MobileBottomNavigation';

export default function App() {
  return (
    <div className="bg-gray-100 min-h-screen pb-16">
      <Header />
      <Promotions />
      <Products />
      <Footer />
      <MobileBottomNavigation />
    </div>
  )
}
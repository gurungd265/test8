import React,{useState,useEffect} from 'react';
import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import MobileBottomNavigation from './components/MobileBottomNavigation';

import ProductPage from './pages/ProductPage';
import CartPage from './pages/CartPage';
import WishesPage from './pages/WishesPage';
import Products from './components/Products';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import authApi from './api/auth';

export default function App() {
    const [loading,setLoading] = useState(true);
    useEffect(() => {
        const autoLogin = async() => {
            const token = localStorage.getItem('jwtToken');
            if(token){
                try{
                    await authApi.validateToken();
                    console.log('自動ログイン成功');
                }catch(error){
                    console.error('自動ログイン失敗：トークン無効',
                        error.response ? error.response.status : error.message);
                    authApi.logout();
                }
            }
            setLoading(false);
        };
        autoLogin();
        },[]);

  return(
    <Router>
      {/* Header */}
      <Header />

      {/* Routing Area */}
      <main className="min-h-screen pb-20">
        <Routes>
          <Route path="/" element={<Products />} />
          <Route path="/product/:id" element={<ProductPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/wishes" element={<WishesPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
        </Routes>
      </main>

      {/* Footer */}
      {/* <Footer /> */}

      {/* Mobile Bottom Navigation */}
      <MobileBottomNavigation />
    </Router>
  )
}
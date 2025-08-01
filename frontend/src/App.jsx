import React from 'react';
import {BrowserRouter as Router, Routes, Route, Navigate, Outlet} from 'react-router-dom';
import Header from './components/Header';
import MobileBottomNavigation from './components/MobileBottomNavigation';
import MobileSearchPage from './pages/MobileSearchPage';
import ProductPage from './pages/ProductPage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import WishesPage from './pages/WishesPage';
import Products from './components/Products';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import { AuthProvider,useAuth } from './contexts/AuthContext';
import { CartProvider } from './contexts/CartContext.jsx';
import { CategoryProvider } from "./contexts/CategoryContext.jsx";
import ProfilePage from './pages/ProfilePage';
import PrivacyPolicyPage from './pages/PrivacyPolicyPage';
import FilteredProductPage from "./pages/FilteredProductPage.jsx";
import MyPointPage from './pages/MyPointPage';
import PaymentRegistrationPage from './pages/PaymentRegistrationPage';

    const ProtectedRoute = ({ requiresAuth = false, onlyUnauthenticated = false, redirectPath = '/' }) => {
        const { isLoggedIn,loading } = useAuth();

        if (loading) {
            return null;
        }

        //case 1 : not login
        if(onlyUnauthenticated){
            if(isLoggedIn) return <Navigate to={redirectPath} replace />; //'replace' no back
            return <Outlet />;
        }

        //case 2 : only login
        if(requiresAuth){
            if(!isLoggedIn) return <Navigate to={redirectPath} replace />;
            return <Outlet />
        }
        //case 3 : public ok
        return <Outlet />;
    };

function AppContent() {

  return (
      <>
        {/* AuthContext */}
      <CartProvider>
        <Header />

        {/* Routing Area */}
        <main className="min-h-screen pb-20">
        {/* public */}
            <Routes>
                <Route element={<ProtectedRoute />}>
                  <Route path="/" element={<Products />} />
                  <Route path="/products" element={<FilteredProductPage />} />
                  <Route path="/product/:id" element={<ProductPage />} />
                  <Route path="/cart" element={<CartPage />} />
                  <Route path="/privacy" element={<PrivacyPolicyPage />} />
                    <Route path="/search" element={<MobileSearchPage />} />
                {/* (Payment/Checkout) 機能はショッピングカート内のアクションか別途ページ
                    もし/checkoutページがあれば、次のように保護:
                    <Route path="/checkout" element={<ProtectedRoute requiresAuth={true} redirectPath="/login"><CheckoutPage /></ProtectedRoute>} />
                    */}
              </Route>

                  {/* not Login */}
                  <Route element={<ProtectedRoute onlyUnauthenticated={true} redirectPath="/" />}>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                  </Route>

                  {/* Need Login */}
                  <Route element={<ProtectedRoute requiresAuth={true} redirectPath="/login" />}>
                    <Route path="/wishes" element={<WishesPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    <Route path="/checkout" element={<CheckoutPage />} />
                    <Route path="/my-points" element={<MyPointPage />} />
                    <Route path="/payment-registration" element={<PaymentRegistrationPage />} />
                    {/*  + login need page(ex: /profile, /dashboard) */}
                    {/* <Route path="/profile" element={<ProfilePage />} /> */}
                    {/* <Route path="/dashboard" element={<DashboardPage />} /> */}
                  </Route>

                  {/* 404 Not Found page */}
                  <Route path="*" element={<div>(404 Not Found)</div>} />
                </Routes>
          </main>
          </CartProvider>

        {/* Footer */}
        {/* <Footer /> */}

        {/* Mobile Bottom Navigation */}
        <MobileBottomNavigation />
      </>
    );
  }

export default function App() {
    return (
      <Router>
          <CategoryProvider>
              <AuthProvider>
                  <AppContent />
                  </AuthProvider>
          </CategoryProvider>
      </Router>
    );
}

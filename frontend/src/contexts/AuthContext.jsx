import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api';
import authApi from '../api/auth';
import cartApi from '../api/cart';
import Cookies from 'js-cookie';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('jwtToken'));
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('jwtToken'));
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userEmail');
    setToken(null);
    setUser(null);
    setIsLoggedIn(false);
    Cookies.remove('sessionId', { path: '/' });
    delete api.defaults.headers.common['Authorization'];
  }, []);

  const validateToken = useCallback(async () => {
    const currentToken = localStorage.getItem('jwtToken');
    if (!currentToken) {
      setIsLoggedIn(false);
      setUser(null);
      setLoading(false);
      return;
    }

    try {
      api.defaults.headers.common['Authorization'] = `Bearer ${currentToken}`;
      const userData = await authApi.validateToken();

      if (userData && userData.email) {
        setUser(userData);
        setIsLoggedIn(true);
      } else {
        logout();
      }
    } catch (error) {
      logout();
    } finally {
      setLoading(false);
    }
  }, [logout]);

  useEffect(() => {
    validateToken();
  }, [validateToken, token]);

  const login = async (email, password) => {
    setLoading(true);

    try {
      const loginData = await authApi.login(email, password);
      const newToken = loginData.token;
      const userEmail = loginData.userEmail;

      localStorage.setItem('jwtToken', newToken);
      localStorage.setItem('userEmail', userEmail || '');

      setToken(newToken);
      await validateToken();

      const anonymousSessionId = Cookies.get('sessionId');
      if(anonymousSessionId){
          console.log('ログイン成功！既存の匿名セッションID:', anonymousSessionId, 'が見つかりました。カートの統合を試みます。');
          try {
              await cartApi.mergeAnonymousCart(anonymousSessionId);
              Cookies.remove('sessionId', { path: '/' });
              console.log('カートが正常に統合され、匿名セッションIDが削除されました。');
          } catch (mergeError) {
              console.error('カート統合に失敗しました。', mergeError);
          }
      }

      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;

      setLoading(false);
      return loginData;
    } catch (error) {
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('userEmail');
      setToken(null);
      setUser(null);
      setIsLoggedIn(false);
      delete api.defaults.headers.common['Authorization'];
      setLoading(false);
      throw error;
    }
  };

  const authContextValue = {
    user,
    isLoggedIn,
    token,
    loading,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={authContextValue}>
      {loading ? (
        <div className="flex justify-center items-center min-h-screen">
          <p>認証情報を読み込み中...</p>
        </div>
      ) : (
        children
      )}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
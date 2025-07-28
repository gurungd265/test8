import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api';
import authApi from '../api/auth';
import cartApi from '../api/cart';
import Cookies from 'js-cookie';
import {jwtDecode} from 'jwt-decode';

export const AuthContext = createContext(null);;

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('jwtToken'));
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('jwtToken'));
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    console.log("ログアウト処理を開始します。");
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userEmail');
    setToken(null);
    setUser(null);
    setIsLoggedIn(false);
    Cookies.remove('sessionId', { path: '/' });
    delete api.defaults.headers.common['Authorization'];
  }, []);

  const isTokenExpired = useCallback((currentToken) => {
      if (!currentToken) {
        return true;
      }
      try {
        const decoded = jwtDecode(currentToken);
        const currentTime = Date.now() / 1000;
        return decoded.exp < currentTime;
      } catch (error) {
        console.error('トークンデコード失敗、または無効なトークン:', error);
        return true;
      }
    },[]);

  const validateToken = useCallback(async () => {
    const currentToken = localStorage.getItem('jwtToken');
    if (!currentToken) {
      setIsLoggedIn(false);
      setUser(null);
      setLoading(false);
      return;
    } else if (isTokenExpired(currentToken)) {
        console.log('クライアント側でJWTトークンが有効ではありません（期限切れ）。');
        logout();
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
  }, [logout,isTokenExpired]);

  useEffect(() => {
      const interceptor = api.interceptors.response.use(
          (response) => response,
          (error) => {
              if (error.response && error.response.status === 401) {
                  console.error('API呼び出し中に401 Unauthorizedエラーが発生しました。ログアウトします。');
                  logout();
                  alert('セッションが切れました。再度ログインしてください。');
                  return Promise.reject(error);
              }
            return Promise.reject(error);
          }
      );

  return () => {
      api.interceptors.response.eject(interceptor);
      };
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
      if (anonymousSessionId) {
          console.log('ログイン成功！既存の匿名セッションIDが見つかりました。カートの統合を試みます。');
          try {
              await cartApi.mergeAnonymousCart(anonymousSessionId);
              console.log('カートが正常に統合されました。');
          } catch (mergeError) {
              console.error('カート統合に失敗しました。', mergeError);
          }
      } else{
          console.log('DEBUG_AUTH: not have anonymousSessionId');
      }

      setLoading(false);
      return loginData;
    } catch (error) {
      setIsLoggedIn(false);
      console.error("ログイン失敗:", error);
      logout();
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
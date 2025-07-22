import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api';
import authApi from '../api/auth';

// 1. AuthContext生成:すべてのコンポーネントがアクセスできるグローバルコンテキスト
const AuthContext = createContext(null);

// 2. AuthProviderコンポーネント:認証状態を管理し、下位コンポーネントに提供
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('jwtToken'));
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('jwtToken'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      setLoading(true);
      if (token) {
        // Axios基本ヘッダーにトークン設定
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        try {
          // トークンの有効性検査とユーザー情報の取得 (バックエンド/api/users/meエンドポイント使用)
          // 「auth Api.validate Token」がJWTトークンを使用して現在ログインされているユーザー情報を返すと仮定
          const userData = await authApi.validateToken();
          setUser(userData);
          setIsLoggedIn(true);
        } catch (error) {
          console.error("既存のトークンでのユーザー情報取得に失敗しました:", error);
          // トークンが有効でない場合はログアウト処理
          logout();
        }
      } else {
        // トークンがなければログアウト状態に初期化
        setUser(null);
        setIsLoggedIn(false);
        delete api.defaults.headers.common['Authorization']; // 残っている可能性のあるAuthorizationヘッダーの除去
      }
      setLoading(false);
    };

    initializeAuth();
  }, [token]); // token値が変更されるたびにエフェクトを再実行

  // ログイン関数:バックエンドログインAPI呼び出し後、ステータス更新
  const login = async (email, password) => {
    try {
      const response = await authApi.login(email, password);
      const newToken = response.data.token;
      const userEmail = response.data.email;

      localStorage.setItem('jwtToken', newToken);
      localStorage.setItem('userEmail', userEmail);
      setToken(newToken); // 状態アップデート -> useEffect再実行誘導
      setIsLoggedIn(true);
      setUser({ email: userEmail });
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`; // Axios基本ヘッダーにトークン設定
      return response.data;
    } catch (error) {
      console.error("ログインに失敗しました:", error);
      throw error;
    }
  };

  // ログアウト関数:ローカルストレージからトークンを削除した後、ステータスを更新
  const logout = () => {
    authApi.logout();
    setToken(null);
    setUser(null);
    setIsLoggedIn(false);
    delete api.defaults.headers.common['Authorization'];
    // navigate('/');
    console.log("ログアウトしました。");
  };

  const authContextValue = {
    user,           // 現在ログインしているユーザー情報
    isLoggedIn,     // (boolean)
    token,          // JWT
    loading,
    login,
    logout,
    // 追加的に必要な関数(例:会員登録、パスワード変更など)
  };

  return (
    <AuthContext.Provider value={authContextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// 3. use Auth カスタム フック: コンテキスト値を簡単に使用するためのフック
export const useAuth = () => {
  const context = useContext(AuthContext);
  // AuthProviderに包まれていないとエラー発生
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
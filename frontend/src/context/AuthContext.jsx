import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('snaplink_jwt_token'));
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('snaplink_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      if (token) {
        try {
          const userData = await authApi.getMe();
          setUser(userData);
          localStorage.setItem('snaplink_user', JSON.stringify(userData));
        } catch (error) {
          console.error('Failed to fetch user info:', error);
          logout();
        }
      }
      setIsLoading(false);
    };

    fetchCurrentUser();
  }, [token]);

  const login = async (email, password) => {
    const data = await authApi.login({ email, password });
    const receivedToken = data.accessToken;
    setToken(receivedToken);
    localStorage.setItem('snaplink_jwt_token', receivedToken);

    try {
      const userData = await authApi.getMe();
      setUser(userData);
      localStorage.setItem('snaplink_user', JSON.stringify(userData));
      return userData;
    } catch {
      const basicUser = { id: data.userId, email: data.email, fullName: data.email.split('@')[0] };
      setUser(basicUser);
      localStorage.setItem('snaplink_user', JSON.stringify(basicUser));
      return basicUser;
    }
  };

  const register = async (email, password, fullName) => {
    const data = await authApi.register({ email, password, fullName });
    const receivedToken = data.accessToken;
    setToken(receivedToken);
    localStorage.setItem('snaplink_jwt_token', receivedToken);

    const basicUser = { id: data.userId, email: data.email, fullName: fullName || data.email.split('@')[0] };
    setUser(basicUser);
    localStorage.setItem('snaplink_user', JSON.stringify(basicUser));
    return basicUser;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('snaplink_jwt_token');
    localStorage.removeItem('snaplink_user');
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        isAuthenticated: !!token,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

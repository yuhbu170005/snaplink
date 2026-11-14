import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { authApi } from '../api/authApi';
import { AUTH_EXPIRED_EVENT, clearSession, readToken, saveSession } from '../utils/authStorage';

const AuthContext = createContext(null);

/** Nguồn state đăng nhập duy nhất cho Navbar, ProtectedRoute và các trang. */
export function AuthProvider({ children }) {
  const [token, setToken] = useState(readToken);
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(Boolean(token));
  const [authError, setAuthError] = useState('');

  const logout = useCallback(() => {
    clearSession();
    setToken(null);
    setUser(null);
    setAuthError('');
    setIsLoading(false);
  }, []);

  useEffect(() => {
    window.addEventListener(AUTH_EXPIRED_EVENT, logout);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, logout);
  }, [logout]);

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    setIsLoading(true);
    setAuthError('');
    // Khi reload trang, xác nhận token với backend thay vì tin dữ liệu user trong storage.
    authApi.getMe()
      .then((currentUser) => { if (!cancelled) setUser(currentUser); })
      .catch((error) => {
        if (!cancelled && error.response?.status !== 401) {
          setAuthError('Không thể xác minh phiên đăng nhập. Vui lòng tải lại trang.');
        }
      })
      .finally(() => { if (!cancelled) setIsLoading(false); });
    // Bỏ response của request cũ khi logout hoặc token đổi trong lúc chờ mạng.
    return () => { cancelled = true; };
  }, [token]);

  function acceptSession(data, rememberMe) {
    saveSession(data.accessToken, rememberMe);
    const account = { id: data.userId, email: data.email };
    setUser(account);
    setToken(data.accessToken);
    return account;
  }

  async function login(email, password, rememberMe = true) {
    const data = await authApi.login({ email, password });
    return acceptSession(data, rememberMe);
  }

  async function register(email, password) {
    const data = await authApi.register({ email, password });
    return acceptSession(data, true);
  }

  return (
    <AuthContext.Provider value={{ token, user, isAuthenticated: Boolean(token && user), isLoading, authError, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

/** Component gọi useAuth() để đọc state và hành động từ provider phía trên. */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}

import axios from 'axios';
import { AUTH_EXPIRED_EVENT, clearSession, readToken } from '../utils/authStorage';

/** Một HTTP client dùng chung; Vite proxy /api ở local, env chỉ rõ API khi deploy. */
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

axiosClient.interceptors.request.use((config) => {
  const token = readToken();
  // Lưu token đã dùng để response của phiên cũ không đăng xuất phiên mới.
  config.sessionToken = token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response.data, // Backend trả DTO trực tiếp, không có lớp wrapper “data”.
  (error) => {
    const requestToken = error.config?.sessionToken;
    if (error.response?.status === 401 && requestToken && requestToken === readToken()) {
      clearSession();
      // Cập nhật cả React state; chỉ xoá storage sẽ khiến Navbar còn báo đã đăng nhập.
      window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    }
    return Promise.reject(error);
  },
);

export default axiosClient;

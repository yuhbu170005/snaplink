import axiosClient from './axiosClient';

// Chỉ mô tả endpoint. State đăng nhập thuộc AuthContext, không đặt trong API module.
export const authApi = {
  login: (credentials) => axiosClient.post('/api/auth/login', credentials),
  register: (userData) => axiosClient.post('/api/auth/register', userData),
  getMe: () => axiosClient.get('/api/auth/me'),
};

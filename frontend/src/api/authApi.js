import axiosClient from './axiosClient';

export const authApi = {
  login: async (credentials) => {
    return await axiosClient.post('/api/auth/login', credentials);
  },

  register: async (userData) => {
    return await axiosClient.post('/api/auth/register', userData);
  },

  getMe: async () => {
    return await axiosClient.get('/api/auth/me');
  },
};

import axiosClient from './axiosClient';

export const urlApi = {
  createUrl: async (payload) => {
    return await axiosClient.post('/api/urls', payload);
  },

  getUrls: async (params = {}) => {
    const { page = 0, size = 10 } = params;
    return await axiosClient.get('/api/urls', {
      params: { page, size },
    });
  },

  getUrlById: async (id) => {
    return await axiosClient.get(`/api/urls/${id}`);
  },

  updateStatus: async (id, status) => {
    return await axiosClient.patch(`/api/urls/${id}/status`, { status });
  },

  deleteUrl: async (id) => {
    return await axiosClient.delete(`/api/urls/${id}`);
  },
};

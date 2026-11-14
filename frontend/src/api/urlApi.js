import axiosClient from './axiosClient';

/** Contract khớp trực tiếp với UrlController và AnalyticsController của backend. */
export const urlApi = {
  createUrl: (payload) => axiosClient.post('/api/urls', payload),
  getUrls: ({ page = 0, size = 10 } = {}, config = {}) =>
    axiosClient.get('/api/urls', { ...config, params: { page, size } }),
  getUrlById: (id, config = {}) => axiosClient.get(`/api/urls/${id}`, config),
  getAnalytics: (id, config = {}) => axiosClient.get(`/api/urls/${id}/analytics`, config),
  // Endpoint hiện tại là toggle: không nhận body status, trả lại isActive mới.
  toggleStatus: (id) => axiosClient.patch(`/api/urls/${id}/status`),
  deleteUrl: (id) => axiosClient.delete(`/api/urls/${id}`),
};

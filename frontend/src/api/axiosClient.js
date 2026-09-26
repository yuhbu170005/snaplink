import axios from 'axios';

const axiosClient = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Token if available
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('snaplink_jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Global error handling
axiosClient.interceptors.response.use(
  (response) => {
    // Backend returns ApiResponse wrapper: { success, message, data, timestamp }
    if (response.data && response.data.data !== undefined) {
      return response.data.data;
    }
    return response.data;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('snaplink_jwt_token');
      localStorage.removeItem('snaplink_user');
      // If we are not on login/register/home, redirect or emit event
    }
    return Promise.reject(error);
  }
);

export default axiosClient;

import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
});

// REQUEST interceptor: attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// RESPONSE interceptor: handle 401 (token expired / unauthorized) globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // If request explicitly opts out of redirect (e.g., simulation tests), do not logout
    if (error.config?.skipAuthRedirect) {
      return Promise.reject(error);
    }

    if (error.response && error.response.status === 401) {
      // Only redirect to login if this was NOT the login request itself
      const isLoginRequest = error.config?.url?.includes('/api/auth/login');
      if (!isLoginRequest) {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('username');
        // Trigger a full page redirect so App.jsx re-evaluates auth state
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;

import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
});

// Request interceptor - attach JWT token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('schemesathi_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle 401
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('schemesathi_token');
      localStorage.removeItem('schemesathi_user');
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data)
};

// Profile API
export const profileApi = {
  get: () => api.get('/profile'),
  save: (data) => api.put('/profile', data),
  update: (data) => api.patch('/profile', data)
};

// Schemes API
export const schemesApi = {
  getAll: (params) => api.get('/schemes', { params }),
  getById: (id) => api.get(`/schemes/${id}`),
  checkEligibility: (id) => api.get(`/schemes/${id}/eligibility`),
  checkAllEligibility: () => api.get('/schemes/eligibility/all'),
  save: (id) => api.post(`/schemes/${id}/save`),
  unsave: (id) => api.delete(`/schemes/${id}/save`),
  getSaved: () => api.get('/schemes/saved'),
  getCategories: () => api.get('/schemes/categories')
};

// AI API
export const aiApi = {
  chat: (message, conversationId) => api.post('/ai/chat', { message, conversationId })
};

// Demo API (no auth required)
export const demoApi = {
  getSchemes: () => api.get('/demo/schemes'),
  getScheme: (id) => api.get(`/demo/schemes/${id}`),
  checkEligibility: (schemeId, profile) => api.post(`/demo/eligibility/${schemeId}`, profile),
  getProfiles: () => api.get('/demo/profiles')
};

export default api;

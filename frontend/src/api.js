import axios from 'axios';

export const getBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;

  if (!envUrl || !envUrl.trim()) {
    if (import.meta.env.PROD) {
      throw new Error(
        'Production configuration error: VITE_API_BASE_URL is not defined. ' +
        'Set VITE_API_BASE_URL to your deployed backend URL (e.g. https://your-backend.onrender.com) in your Vercel Project Settings.'
      );
    }
    // Practical local development fallback using Vite dev-server proxy
    return '/api';
  }

  const trimmed = envUrl.trim();
  let parsed;
  try {
    parsed = new URL(trimmed);
  } catch {
    throw new Error(`Invalid VITE_API_BASE_URL: "${trimmed}". Must be a valid absolute HTTP or HTTPS URL.`);
  }

  if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
    throw new Error(`Invalid VITE_API_BASE_URL protocol: "${parsed.protocol}". Must be http: or https:.`);
  }
  if (!parsed.hostname) {
    throw new Error(`Invalid VITE_API_BASE_URL: "${trimmed}". Must include a valid hostname.`);
  }

  // Avoid accidentally appending /api twice
  const cleanUrl = trimmed.replace(/\/+$/, '');
  return cleanUrl.endsWith('/api') ? cleanUrl : `${cleanUrl}/api`;
};

const api = axios.create({
  baseURL: getBaseUrl(),
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
  checkEligibility: (id, memberId) => api.get(`/schemes/${id}/eligibility`, { params: memberId ? { memberId } : {} }),
  checkAllEligibility: (memberId) => api.get('/schemes/eligibility/all', { params: memberId ? { memberId } : {} }),
  evaluateCriteria: (id, criteria) => api.post(`/schemes/${id}/eligibility`, criteria),
  evaluateAllCriteria: (criteria) => api.post('/schemes/eligibility/all', criteria),
  save: (id) => api.post(`/schemes/${id}/save`),
  unsave: (id) => api.delete(`/schemes/${id}/save`),
  getSaved: () => api.get('/schemes/saved'),
  getCategories: () => api.get('/schemes/categories')
};

// Household Members API
export const householdApi = {
  getAll: () => api.get('/household'),
  getById: (id) => api.get(`/household/${id}`),
  create: (data) => api.post('/household', data),
  update: (id, data) => api.put(`/household/${id}`, data),
  delete: (id) => api.delete(`/household/${id}`),
  checkEligibility: (id) => api.get(`/household/${id}/eligibility/all`),
  checkSchemeEligibility: (id, schemeId) => api.get(`/household/${id}/eligibility/${schemeId}`)
};

// Scheme Application Tracking API
export const applicationsApi = {
  getAll: () => api.get('/applications'),
  getSummary: () => api.get('/applications/summary'),
  getById: (id) => api.get(`/applications/${id}`),
  create: (data) => api.post('/applications', data),
  update: (id, data) => api.put(`/applications/${id}`, data),
  delete: (id) => api.delete(`/applications/${id}`),
  getByScheme: (schemeId) => api.get(`/applications/scheme/${schemeId}`)
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
  checkAllEligibility: (profile) => api.post('/demo/eligibility/all', profile),
  getProfiles: () => api.get('/demo/profiles')
};

// Admin Scheme Management & Verification API (Requires ROLE_ADMIN)
export const adminApi = {
  getSchemes: (params) => api.get('/admin/schemes', { params }),
  getStats: () => api.get('/admin/schemes/stats'),
  getSchemeById: (id) => api.get(`/admin/schemes/${id}`),
  createScheme: (data) => api.post('/admin/schemes', data),
  updateScheme: (id, data) => api.put(`/admin/schemes/${id}`, data),
  verifyScheme: (id, data) => api.patch(`/admin/schemes/${id}/verify`, data),
  publishScheme: (id) => api.patch(`/admin/schemes/${id}/publish`),
  archiveScheme: (id) => api.patch(`/admin/schemes/${id}/archive`),
  deleteScheme: (id) => api.delete(`/admin/schemes/${id}`)
};

export default api;


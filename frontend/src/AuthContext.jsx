import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi, profileApi } from './api';

const AuthContext = createContext(null);

function getInitialUser() {
  try {
    const stored = localStorage.getItem('schemesathi_user');
    const token = localStorage.getItem('schemesathi_token');
    if (stored && token) {
      return JSON.parse(stored);
    }
  } catch {
    localStorage.removeItem('schemesathi_token');
    localStorage.removeItem('schemesathi_user');
  }
  return null;
}

function extractErrorMessage(err, fallback) {
  if (!err) return fallback;

  // 1. Network / connection dropped errors
  if (err.code === 'ERR_NETWORK' || err.message === 'Network Error') {
    return 'Unable to reach backend server. Please verify that the backend is running on port 8080.';
  }

  const status = err.response?.status;

  // 2. Gateway / Proxy errors (Vite proxy, Nginx, Render cold-start)
  if (status === 502 || status === 503 || status === 504) {
    return `Backend server is unreachable (HTTP ${status}). Please ensure the SchemeSathi backend server is running on port 8080.`;
  }

  // 3. 500 Proxy connection refused
  if (status === 500 && typeof err.response?.data === 'string' &&
      (err.response.data.includes('ECONNREFUSED') || err.response.data.includes('Proxy error'))) {
    return 'Backend server is unreachable on port 8080 (Connection Refused). Please ensure the backend is running.';
  }

  // 4. 404 Endpoint not found
  if (status === 404) {
    return 'Backend API endpoint not found (HTTP 404). Please verify that the backend server is running.';
  }

  // 5. Backend structured ApiResponse error messages
  if (err.response?.data?.message && typeof err.response.data.message === 'string' && err.response.data.message.trim().length > 0) {
    return err.response.data.message.trim();
  }
  if (err.response?.data?.error && typeof err.response.data.error === 'string' && err.response.data.error.trim().length > 0) {
    return err.response.data.error.trim();
  }

  // 6. Plain text error responses (MUST NOT be empty or HTML)
  if (typeof err.response?.data === 'string' &&
      err.response.data.trim().length > 0 &&
      !err.response.data.trim().startsWith('<')) {
    return err.response.data.trim();
  }

  // 7. Standard error message
  if (err.message && typeof err.message === 'string' && err.message.trim().length > 0 && !err.message.includes('object Object')) {
    return err.message.trim();
  }

  return fallback;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getInitialUser);
  const [profile, setProfile] = useState(null);
  const [loading] = useState(false);

  const loadProfile = useCallback(async () => {
    try {
      const res = await profileApi.get();
      if (res.data?.success) {
        setProfile(res.data.data);
      }
    } catch {
      // Profile may not exist yet
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('schemesathi_token');
    localStorage.removeItem('schemesathi_user');
    setUser(null);
    setProfile(null);
  }, []);

  useEffect(() => {
    if (user) {
      loadProfile();
    }
  }, [user, loadProfile]);

  const login = async (email, password) => {
    try {
      const res = await authApi.login({ email, password });
      if (typeof res.data === 'string' && res.data.includes('<!doctype html>')) {
        throw new Error('Backend server is unreachable. Received HTML instead of API response.');
      }
      if (res.data?.success) {
        const userData = res.data.data;
        localStorage.setItem('schemesathi_token', userData.token);
        localStorage.setItem('schemesathi_user', JSON.stringify(userData));
        setUser(userData);
        await loadProfile();
        return userData;
      }
      throw new Error(res.data?.message || 'Login failed');
    } catch (err) {
      throw new Error(extractErrorMessage(err, 'Login failed'));
    }
  };

  const register = async (fullName, email, password, phone) => {
    try {
      const res = await authApi.register({ fullName, email, password, phone });
      if (typeof res.data === 'string' && res.data.includes('<!doctype html>')) {
        throw new Error('Backend server is unreachable. Received HTML instead of API response.');
      }
      if (res.data?.success) {
        const userData = res.data.data;
        localStorage.setItem('schemesathi_token', userData.token);
        localStorage.setItem('schemesathi_user', JSON.stringify(userData));
        setUser(userData);
        return userData;
      }
      throw new Error(res.data?.message || 'Registration failed');
    } catch (err) {
      throw new Error(extractErrorMessage(err, 'Registration failed'));
    }
  };

  const updateProfile = (newProfile) => {
    setProfile(newProfile);
  };

  return (
    <AuthContext.Provider value={{ user, profile, loading, login, register, logout, loadProfile, updateProfile }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

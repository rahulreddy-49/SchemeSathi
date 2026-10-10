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
      const message = err.response?.data?.message || err.message || 'Login failed';
      throw new Error(message);
    }
  };

  const register = async (fullName, email, password, phone) => {
    try {
      const res = await authApi.register({ fullName, email, password, phone });
      if (res.data?.success) {
        const userData = res.data.data;
        localStorage.setItem('schemesathi_token', userData.token);
        localStorage.setItem('schemesathi_user', JSON.stringify(userData));
        setUser(userData);
        return userData;
      }
      throw new Error(res.data?.message || 'Registration failed');
    } catch (err) {
      const message = err.response?.data?.message || err.message || 'Registration failed';
      throw new Error(message);
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

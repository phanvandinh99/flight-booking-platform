import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('fb_user');
    return raw ? JSON.parse(raw) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('fb_token'));

  useEffect(() => {
    if (user) {
      localStorage.setItem('fb_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('fb_user');
    }
  }, [user]);

  useEffect(() => {
    if (token) {
      localStorage.setItem('fb_token', token);
    } else {
      localStorage.removeItem('fb_token');
    }
  }, [token]);

  const login = (userData, accessToken) => {
    setUser(userData);
    setToken(accessToken);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
  };

  const value = useMemo(() => ({ user, token, login, logout }), [user, token]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}






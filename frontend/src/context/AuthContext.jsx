import { createContext, useContext, useState, useEffect } from 'react';
import { isAuthenticated, getAuthToken, getUser, setAuthToken, setUser, clearAuth } from '../utils/auth';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUserState] = useState(null);
  const [loading, setLoading] = useState(true);

  // Check authentication status on mount and when needed
  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = () => {
    const authenticated = isAuthenticated();
    const authToken = getAuthToken();
    const userData = getUser();
    
    setIsLoggedIn(authenticated);
    setUserState(userData);
    setLoading(false);
  };

  const login = (token, userData) => {
    setAuthToken(token);
    if (userData) {
      setUser(userData);
    }
    setIsLoggedIn(true);
    setUserState(userData);
  };

  const logout = () => {
    clearAuth();
    setIsLoggedIn(false);
    setUserState(null);
  };

  const value = {
    isLoggedIn,
    user,
    loading,
    login,
    logout,
    checkAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

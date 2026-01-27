import Cookies from 'js-cookie';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

/**
 * Set authentication token in cookie
 * @param {string} token - Authentication token
 */
export const setAuthToken = (token) => {
  // Set cookie with 7 days expiration
  // secure: false for development (set to true in production with HTTPS)
  Cookies.set(TOKEN_KEY, token, { expires: 7, secure: false, sameSite: 'lax' });
};

/**
 * Get authentication token from cookie
 * @returns {string|null} - Authentication token or null
 */
export const getAuthToken = () => {
  return Cookies.get(TOKEN_KEY) || null;
};

/**
 * Set user data in cookie
 * @param {object} user - User data object
 */
export const setUser = (user) => {
  // secure: false for development (set to true in production with HTTPS)
  Cookies.set(USER_KEY, JSON.stringify(user), { expires: 7, secure: false, sameSite: 'lax' });
};

/**
 * Get user data from cookie
 * @returns {object|null} - User data or null
 */
export const getUser = () => {
  const userStr = Cookies.get(USER_KEY);
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch (e) {
      return null;
    }
  }
  return null;
};

/**
 * Check if user is authenticated
 * @returns {boolean} - True if user is authenticated
 */
export const isAuthenticated = () => {
  return !!getAuthToken();
};

/**
 * Clear authentication data (logout)
 */
export const clearAuth = () => {
  Cookies.remove(TOKEN_KEY);
  Cookies.remove(USER_KEY);
};

/**
 * Get axios config with auth token
 * @returns {object} - Axios config object
 */
export const getAuthConfig = () => {
  const token = getAuthToken();
  return {
    headers: {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json',
    },
    withCredentials: true, // Important for session cookies
  };
};

/**
 * Get dashboard route based on user role
 * @param {string} role - User role (ADMIN, MAKER, CHECKER, CUSTOMER)
 * @returns {string} - Dashboard route path
 */
export const getDashboardRoute = (role) => {
  switch (role) {
    case 'ADMIN':
      return '/admin';
    case 'MAKER':
      return '/maker/dashboard';
    case 'CHECKER':
      return '/checker/dashboard';
    case 'CUSTOMER':
    default:
      return '/dashboard';
  }
};

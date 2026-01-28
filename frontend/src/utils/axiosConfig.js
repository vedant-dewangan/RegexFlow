import axios from 'axios';
import { clearAuth } from './auth';
import toast from 'react-hot-toast';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';
axios.defaults.baseURL = API_BASE;
axios.defaults.withCredentials = true;
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Global logout callback - will be set by AuthContext
let globalLogoutCallback = null;
let isInterceptorSetup = false;

/**
 * Set the global logout callback
 * @param {Function} callback - The logout function from AuthContext
 */
export const setLogoutCallback = (callback) => {
  globalLogoutCallback = callback;
};

/**
 * Setup axios interceptors for handling token expiration
 * This should be called once when the app initializes
 */
export const setupAxiosInterceptors = () => {
  if (isInterceptorSetup) {
    return; // Already setup
  }

  // Response interceptor to handle token expiration
  axios.interceptors.response.use(
    (response) => {
      // If the request was successful, just return the response
      return response;
    },
    (error) => {
      // Handle response errors
      if (error.response) {
        const status = error.response.status;
        const requestUrl = error.config?.url || '';
        
        // Skip session expiration handling for login endpoint
        // Login failures should be handled by the Login component
        const isLoginEndpoint = requestUrl.includes('/auth/login');
        
        // Check if the error is due to expired/invalid token or unauthorized access
        if ((status === 401 || status === 403) && !isLoginEndpoint) {
          // Prevent multiple logout calls
          if (globalLogoutCallback && !error.config._retry) {
            error.config._retry = true;
            
            // Clear authentication data
            clearAuth();
            
            // Show notification
            toast.error('Your session has expired. Please login again.');
            
            // Call the global logout callback
            globalLogoutCallback();
          } else if (!globalLogoutCallback) {
            // Fallback: redirect to login page if callback is not set
            clearAuth();
            toast.error('Your session has expired. Please login again.');
            setTimeout(() => {
              window.location.href = '/login';
            }, 1000);
          }
        }
      }
      
      // Return the error so it can be handled by the calling code if needed
      return Promise.reject(error);
    }
  );

  isInterceptorSetup = true;
};

// Export default axios for convenience (with interceptors setup)
export default axios;

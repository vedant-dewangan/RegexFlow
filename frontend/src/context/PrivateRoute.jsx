import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * PrivateRoute - Protects routes that require authentication
 * Redirects to login if user is not authenticated
 * @param {React.Component} children - The component to render if user is authenticated
 */
function PrivateRoute({ children }) {
  const { isLoggedIn, loading } = useAuth();

  // Show loading state while checking authentication
  if (loading) {
    return <div>Loading...</div>; // You can replace this with a proper loading component
  }

  if (!isLoggedIn) {
    // If user is not logged in, redirect to login page
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default PrivateRoute;

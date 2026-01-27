import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { getDashboardRoute } from '../utils/auth';

/**
 * RoleBasedRoute - Protects routes that require specific user roles
 * Redirects to login if not authenticated, or to user's dashboard if wrong role
 * @param {React.Component} children - The component to render if user has correct role
 * @param {string|string[]} allowedRoles - Single role or array of roles that can access this route
 */
function RoleBasedRoute({ children, allowedRoles }) {
  const { isLoggedIn, user, loading } = useAuth();

  // Show loading state while checking authentication
  if (loading) {
    return <div>Loading...</div>;
  }

  // If not logged in, redirect to login
  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }

  // Convert single role to array for easier checking
  const roles = Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles];
  const userRole = user?.role;

  // Check if user's role is allowed
  if (!userRole || !roles.includes(userRole)) {
    // Redirect to user's appropriate dashboard if they don't have access
    const dashboardRoute = getDashboardRoute(userRole);
    return <Navigate to={dashboardRoute} replace />;
  }

  return children;
}

export default RoleBasedRoute;

import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { getDashboardRoute } from '../utils/auth';

/**
 * PublicRoute - Redirects authenticated users away from public pages (login/register)
 * If user is already logged in, redirects to their role-specific dashboard
 * @param {React.Component} children - The component to render if user is not authenticated
 */
function PublicRoute({ children }) {
  const { isLoggedIn, user, loading } = useAuth();

  // Show loading state while checking authentication
  if (loading) {
    return <div>Loading...</div>; // You can replace this with a proper loading component
  }

  if (isLoggedIn) {
    // If user is already logged in, redirect to their role-specific dashboard
    const userRole = user?.role;
    const dashboardRoute = getDashboardRoute(userRole);
    return <Navigate to={dashboardRoute} replace />;
  }

  return children;
}

export default PublicRoute;

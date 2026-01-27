import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';
import moneyviewLogo from '../../assets/moneyview_full_logo.svg';
import { useAuth } from '../../context/AuthContext';
import { getDashboardRoute } from '../../utils/auth';

function Navbar() {
  const { isLoggedIn, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Get the dashboard route based on user role
  const getDashboardLink = () => {
    if (!isLoggedIn) return '/login';
    return getDashboardRoute(user?.role);
  };

  // Show "Welcome {name}" if logged in, otherwise show "Dashboard"
  const getDashboardText = () => {
    if (isLoggedIn && user?.name) {
      return `Welcome ${user.name}`;
    }
    return 'Dashboard';
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-left">
          <a 
            href="https://moneyview.in" 
            target="_blank" 
            rel="noopener noreferrer"
            className="navbar-logo-link"
          >
            <img 
              src={moneyviewLogo} 
              alt="MoneyView" 
              className="navbar-logo"
            />
          </a>
          <div className="navbar-divider"></div>
          <Link to="/" className="navbar-brand">
            RegexFlow
          </Link>
        </div>
        <div className="navbar-right">
          <Link 
            to={getDashboardLink()} 
            className="navbar-btn navbar-btn-dashboard"
          >
            {getDashboardText()}
          </Link>
          {isLoggedIn ? (
            <button onClick={handleLogout} className="navbar-btn navbar-btn-logout">
              Logout
            </button>
          ) : (
            <Link to="/login" className="navbar-btn navbar-btn-login">
              Login
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;

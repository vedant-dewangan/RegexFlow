import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import './Navbar.css';
import moneyviewLogo from '../../assets/moneyview_full_logo.svg';

function Navbar() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // Check if user is logged in (check localStorage for token or user data)
    const token = localStorage.getItem('token') || localStorage.getItem('userToken');
    const user = localStorage.getItem('user');
    setIsLoggedIn(!!(token || user));
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userToken');
    localStorage.removeItem('user');
    setIsLoggedIn(false);
    // Optionally redirect to landing page
    window.location.href = '/';
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

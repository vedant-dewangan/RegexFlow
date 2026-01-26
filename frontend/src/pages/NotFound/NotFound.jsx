import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './NotFound.css';

function NotFound() {
  return (
    <div className="not-found-container">
      <div className="not-found-content">
        <h1 className="not-found-title">404</h1>
        <h2 className="not-found-subtitle">Page Not Found</h2>
        <p className="not-found-description">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <div className="not-found-links">
          <a href="https://moneyview.in" target="_blank" rel="noopener noreferrer" className="btn btn-primary">
            Visit MoneyView
          </a>
          <Link to="/" className="btn btn-secondary">
            Go to Home
          </Link>
        </div>
      </div>
    </div>
  );
}

export default NotFound;

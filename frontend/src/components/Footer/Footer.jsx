import { Link } from 'react-router-dom';
import './Footer.css';

function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <div className="footer-section">
            <h4 className="footer-title">RegexFlow</h4>
            <p className="footer-description">
              MoneyView's intelligent SMS-to-Ledger engine for transforming 
              unstructured financial alerts into structured data.
            </p>
          </div>
          <div className="footer-section">
            <h4 className="footer-title">Product</h4>
            <ul className="footer-links">
              <li><Link to="/">Home</Link></li>
              <li><Link to="/login">Login</Link></li>
              <li><Link to="/register">Register</Link></li>
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">MoneyView</a></li>
            </ul>
          </div>
          <div className="footer-section">
            <h4 className="footer-title">Resources</h4>
            <ul className="footer-links">
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">Documentation</a></li>
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">Support</a></li>
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">Privacy Policy</a></li>
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">Terms of Service</a></li>
            </ul>
          </div>
          <div className="footer-section">
            <h4 className="footer-title">Company</h4>
            <ul className="footer-links">
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">About MoneyView</a></li>
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">Contact Us</a></li>
              <li><a href="https://moneyview.in" target="_blank" rel="noopener noreferrer">Careers</a></li>
            </ul>
          </div>
        </div>
        <div className="footer-bottom">
          <p className="footer-copyright">
            © {new Date().getFullYear()} MoneyView. All rights reserved. RegexFlow is an internal tool.
          </p>
        </div>
      </div>
    </footer>
  );
}

export default Footer;

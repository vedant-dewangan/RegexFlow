import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import FAQ from '../../components/FAQ/FAQ';
import Footer from '../../components/Footer/Footer';
import bannerImage from '../../assets/ic-app-banner.webp';
import './Landing.css';
import { useEffect } from 'react';
import axios from 'axios';

function Landing() {

  useEffect(()=>{
    const fetchData = async () => {
    const {data} = await axios.get('http://localhost:8080/');
    console.log(data);
    
    }
    fetchData();
  },[])

  return (
    <div className="landing-page">
      <Navbar />

      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-container">
          <div className="hero-content">
            <h1 className="hero-title">
              Transform SMS Alerts into
              <span className="hero-title-highlight"> Structured Financial Data</span>
            </h1>
            <p className="hero-subtitle">
              RegexFlow is MoneyView's intelligent SMS-to-Ledger engine that automatically 
              converts unstructured bank transaction alerts into structured, actionable financial data.
            </p>
            <div className="hero-actions">
              <Link to="/register" className="btn btn-primary">
                Get Started
              </Link>
              <Link to="/login" className="btn btn-secondary">
                Sign In
              </Link>
            </div>
          </div>
          <div className="hero-visual">
            <div className="hero-card">
              <div className="hero-card-header">
                <div className="hero-card-dot"></div>
                <div className="hero-card-dot"></div>
                <div className="hero-card-dot"></div>
              </div>
              <div className="hero-card-content">
                <div className="hero-card-line"></div>
                <div className="hero-card-line short"></div>
                <div className="hero-card-line"></div>
                <div className="hero-card-line short"></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <div className="container">
          <h2 className="section-title">Powerful Features for Financial Data Management</h2>
          <p className="section-subtitle">
            Everything you need to manage regex patterns, track transactions, and maintain accurate financial records
          </p>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">🔍</div>
              <h3 className="feature-title">Smart Pattern Recognition</h3>
              <p className="feature-description">
                Advanced regex pattern matching to extract transaction details from SMS alerts 
                with high accuracy and reliability.
              </p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">🏦</div>
              <h3 className="feature-title">Multi-Bank Support</h3>
              <p className="feature-description">
                Support for multiple banks with customizable regex templates for each 
                financial institution's unique SMS format.
              </p>
            </div>
            <div className="feature-card">
              <div className="feature-icon">✅</div>
              <h3 className="feature-title">Audit & Verification</h3>
              <p className="feature-description">
                Maker-Checker workflow ensures data accuracy with comprehensive audit trails 
                and verification processes.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* App Banner Section */}
      <section className="app-banner-section">
        <div className="container">
          <div className="app-banner-content">
            <div className="app-banner-image">
              <img src={bannerImage} alt="MoneyView App" />
            </div>
            <div className="app-banner-text">
              <h2 className="app-banner-title">Download MoneyView App</h2>
              <p className="app-banner-description">
                Track and save your money with our smart personal finance tracker. 
                Get real-time insights into your spending, manage your budget, and 
                take control of your financial future.
              </p>
              <div className="app-banner-actions">
                <a 
                  href="https://apps.apple.com/in/app/moneyview-loans-credit-cards/id6468976019" 
                  target="_blank" 
                  rel="noopener noreferrer"
                  className="btn btn-primary btn-large"
                >
                  Download Now
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="how-it-works-section">
        <div className="container">
          <h2 className="section-title">How RegexFlow Works</h2>
          <div className="steps-container">
            <div className="step-item">
              <div className="step-number">1</div>
              <h3 className="step-title">Receive SMS Alert</h3>
              <p className="step-description">
                Bank sends transaction SMS alert to your registered mobile number
              </p>
            </div>
            <div className="step-arrow">→</div>
            <div className="step-item">
              <div className="step-number">2</div>
              <h3 className="step-title">Pattern Matching</h3>
              <p className="step-description">
                RegexFlow matches the SMS against configured regex patterns for your bank
              </p>
            </div>
            <div className="step-arrow">→</div>
            <div className="step-item">
              <div className="step-number">3</div>
              <h3 className="step-title">Data Extraction</h3>
              <p className="step-description">
                Transaction details are extracted: amount, date, type, merchant, and balance
              </p>
            </div>
            <div className="step-arrow">→</div>
            <div className="step-item">
              <div className="step-number">4</div>
              <h3 className="step-title">Ledger Entry</h3>
              <p className="step-description">
                Structured data is created and verified through Maker-Checker workflow
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <div className="container">
          <div className="cta-content">
            <h2 className="cta-title">Ready to Transform Your Financial Data Management?</h2>
            <p className="cta-subtitle">
              Join MoneyView's internal team and start processing SMS alerts with precision and efficiency
            </p>
            <div className="cta-actions">
              <Link to="/register" className="btn btn-primary btn-large">
                Get Started Now
              </Link>
              <Link to="/login" className="btn btn-outline btn-large">
                Sign In to Your Account
              </Link>
            </div>
          </div>
        </div>
      </section>

      <FAQ />
      <Footer />
    </div>
  );
}

export default Landing;

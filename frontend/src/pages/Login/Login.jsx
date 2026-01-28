import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import FormInput from '../../components/FormInput/FormInput';
import toast from 'react-hot-toast';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';
import { getDashboardRoute } from '../../utils/auth';
import './Login.css';

function Login() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate all required fields
    if (!formData.email || !formData.email.trim()) {
      toast.error('Email is required and cannot be empty');
      return;
    }

    if (!formData.password || !formData.password.trim()) {
      toast.error('Password is required and cannot be empty');
      return;
    }
    
    setIsLoading(true);
    
    try {
      const response = await axios.post(
        '/auth/login',
        {
          email: formData.email,
          password: formData.password,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
          withCredentials: true, // Important for session cookies
        }
      );

      if (response.data && response.data.token) {
        // Use AuthContext to update auth state
        login(response.data.token, response.data.user);
        
        // Navigate based on user role
        const userRole = response.data.user?.role;
        const dashboardRoute = getDashboardRoute(userRole);
        
        toast.success(response.data.message || 'Login successful');
        navigate(dashboardRoute);
      } else {
        toast.error('Login failed: Invalid response from server');
      }
    } catch (error) {
      console.error('Login error:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Login failed';
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <Navbar />
      <div className="login-card">
        <div className="login-header">
          <h1 className="login-title">Welcome Back</h1>
          <p className="login-subtitle">Sign in to your account to continue</p>
        </div>
        <form className="login-form" onSubmit={handleSubmit}>
          <FormInput
            label="Email Address"
            type="email"
            id="email"
            name="email"
            placeholder="Enter your email"
            value={formData.email}
            onChange={handleChange}
            required
          />
          <FormInput
            label="Password"
            type="password"
            id="password"
            name="password"
            placeholder="Enter your password"
            value={formData.password}
            onChange={handleChange}
            required
            showPasswordToggle
          />
          <button type="submit" className="btn-submit" disabled={isLoading}>
            {isLoading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>
        <p className="login-footer">
          Don't have an account? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  );
}

export default Login;

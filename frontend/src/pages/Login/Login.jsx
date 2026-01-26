import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import FormInput from '../../components/FormInput/FormInput';
import toast from 'react-hot-toast';
import './Login.css';

function Login() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });

  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    try {
      console.log('Login data:', formData);
      toast.success('Login successful');
      navigate("/dashboard");
    } catch (error) {
      console.log(error);
      toast.error('Login failed');
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
          <button type="submit" className="btn-submit">
            Sign In
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

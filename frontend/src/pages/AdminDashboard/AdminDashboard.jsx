import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './AdminDashboard.css';

function AdminDashboard() {
  return (
    <div className="admin-dashboard-container">
      <Navbar />
      <div className="admin-dashboard-header">
        <h1>Admin Dashboard</h1>
        <p>System administration and management</p>
      </div>
      <div className="admin-dashboard-content">
        <div className="admin-dashboard-grid">
          <Link to="/admin/user" className="admin-card">
            <h2>User Management</h2>
            <p>Manage users and assign roles (Maker/Checker)</p>
          </Link>
          <Link to="/admin/bank" className="admin-card">
            <h2>Bank Management</h2>
            <p>Add and manage banks</p>
          </Link>
          <Link to="/admin/regex" className="admin-card">
            <h2>Regex Monitoring</h2>
            <p>Monitor all regex templates and their status</p>
          </Link>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;

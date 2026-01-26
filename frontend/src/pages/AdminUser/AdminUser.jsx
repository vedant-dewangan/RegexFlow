import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './AdminUser.css';

function AdminUser() {
  return (
    <div className="admin-user-container">
      <Navbar />
      <div className="admin-user-header">
        <h1>User Management</h1>
        <p>Manage all users and assign roles</p>
      </div>
      <div className="admin-user-content">
        <div className="admin-user-card">
          <h2>All Users</h2>
          <p>View and manage user roles (Maker/Checker)</p>
        </div>
      </div>
    </div>
  );
}

export default AdminUser;

import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './AdminRegex.css';

function AdminRegex() {
  return (
    <div className="admin-regex-container">
      <Navbar />
      <div className="admin-regex-header">
        <h1>Regex Monitoring</h1>
        <p>Monitor all regex templates, their status, creators, and approvers</p>
      </div>
      <div className="admin-regex-content">
        <div className="admin-regex-card">
          <h2>All Regex Templates</h2>
          <p>View all templates with their status, maker, and checker information</p>
        </div>
      </div>
    </div>
  );
}

export default AdminRegex;

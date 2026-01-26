import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './AdminBank.css';

function AdminBank() {
  return (
    <div className="admin-bank-container">
      <Navbar />
      <div className="admin-bank-header">
        <h1>Bank Management</h1>
        <p>Add and manage banks</p>
      </div>
      <div className="admin-bank-content">
        <div className="admin-bank-card">
          <h2>Banks</h2>
          <p>Add new banks to the system</p>
        </div>
      </div>
    </div>
  );
}

export default AdminBank;

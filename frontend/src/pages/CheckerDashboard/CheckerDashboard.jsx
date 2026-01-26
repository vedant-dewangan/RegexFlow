import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import './CheckerDashboard.css';

function CheckerDashboard() {
  return (
    <div className="checker-dashboard-container">
      <Navbar />
      <div className="checker-dashboard-header">
        <h1>Checker Dashboard</h1>
        <p>Review and approve regex templates</p>
      </div>
      <div className="checker-dashboard-content">
        <div className="checker-dashboard-card">
          <h2>Approval Queue</h2>
          <p>Review pending templates for approval.</p>
        </div>
        <div className="checker-dashboard-card">
          <h2>Approved Templates</h2>
          <p>View all approved templates.</p>
        </div>
      </div>
    </div>
  );
}

export default CheckerDashboard;

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import './MakerDashboard.css';

function MakerDashboard() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (user?.userId) {
      fetchTemplates();
    } else {
      setLoading(false);
    }
  }, [user?.userId]);

  const fetchTemplates = async () => {
    if (!user?.userId) return;
    try {
      setLoading(true);
      const response = await axios.get(`/regex/${user.userId}`);
      setTemplates(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching templates:', error);
      toast.error(error.response?.status === 401 ? 'Please log in again' : 'Failed to load templates');
      setTemplates([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTemplate = () => {
    navigate('/maker/template/new');
  };

  const handleEditTemplate = (templateId) => {
    navigate(`/maker/template/${templateId}`);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'DRAFT':
        return '#ffa500';
      case 'PENDING':
        return '#2196F3';
      case 'VERIFIED':
        return '#4CAF50';
      case 'DEPRECATED':
        return '#f44336';
      default:
        return '#666';
    }
  };

  return (
    <div className="maker-dashboard-container">
      <Navbar />
      <div className="maker-dashboard-wrapper">
        <div className="maker-dashboard-header">
          <h1>Maker Dashboard</h1>
          <p>Create and manage regex templates</p>
        </div>
        
        <div className="maker-dashboard-actions">
          <button 
            className="btn-create-template"
            onClick={handleCreateTemplate}
          >
            + Create New Template
          </button>
        </div>

        <div className="maker-dashboard-content">
          <div className="templates-section">
            <h2>My Templates</h2>
            {loading ? (
              <div className="loading-state">Loading templates...</div>
            ) : templates.length === 0 ? (
              <div className="empty-state">
                <p>No templates found.</p>
                <p className="empty-state-hint">Check here to make your first template</p>
                <button 
                  className="btn-create-first"
                  onClick={handleCreateTemplate}
                >
                  Create Your First Template
                </button>
              </div>
            ) : (
              <div className="templates-list">
                {templates.map((template) => (
                  <div key={template.templateId} className="template-card">
                    <div className="template-header">
                      <div className="template-title">
                        <h3>{template.senderHeader}</h3>
                        <span 
                          className="status-badge"
                          style={{ backgroundColor: getStatusColor(template.status) }}
                        >
                          {template.status}
                        </span>
                      </div>
                      {template.status === 'DRAFT' && (
                        <button
                          className="btn-edit-template"
                          onClick={() => handleEditTemplate(template.templateId)}
                        >
                          Edit
                        </button>
                      )}
                    </div>
                    <div className="template-details">
                      <div className="template-detail-item">
                        <span className="detail-label">Pattern:</span>
                        <span className="detail-value">{template.pattern}</span>
                      </div>
                      {template.sampleRawMsg && (
                        <div className="template-detail-item">
                          <span className="detail-label">Sample raw msg:</span>
                          <span className="detail-value" title={template.sampleRawMsg}>
                            {template.sampleRawMsg.length > 80
                              ? `${template.sampleRawMsg.slice(0, 80)}…`
                              : template.sampleRawMsg}
                          </span>
                        </div>
                      )}
                      <div className="template-detail-item">
                        <span className="detail-label">SMS Type:</span>
                        <span className="detail-value">{template.smsType}</span>
                      </div>
                      <div className="template-detail-item">
                        <span className="detail-label">Payment Type:</span>
                        <span className="detail-value">{template.paymentType}</span>
                      </div>
                      <div className="template-detail-item">
                        <span className="detail-label">Bank:</span>
                        <span className="detail-value">{template.bankName || 'N/A'}</span>
                      </div>
                      <div className="template-detail-item">
                        <span className="detail-label">Created:</span>
                        <span className="detail-value">
                          {new Date(template.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default MakerDashboard;

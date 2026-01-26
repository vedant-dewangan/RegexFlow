import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import './MakerDashboard.css';

function MakerDashboard() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = async () => {
    try {
      setLoading(true);
      // TODO: Replace with actual API endpoint
      // const response = await axios.get('/api/templates/my-templates');
      // setTemplates(response.data);
      
      // Sample data with 8 templates
      const mockTemplates = [
        {
          templateId: 1,
          senderHeader: 'AXISBK',
          pattern: 'Rs\\.(\\d+\\.?\\d*)\\s+debited\\s+from\\s+A/c\\s+(\\d+)',
          status: 'DRAFT',
          smsType: 'DEBIT',
          paymentType: 'UPI',
          bankId: 1,
          bankName: 'Axis Bank',
          createdAt: '2025-01-20T10:30:00'
        },
        {
          templateId: 2,
          senderHeader: 'HDFCBK',
          pattern: 'INR\\s+(\\d+\\.?\\d*)\\s+credited\\s+to\\s+A/c\\s+(\\d+)',
          status: 'PENDING',
          smsType: 'CREDIT',
          paymentType: 'UPI',
          bankId: 2,
          bankName: 'HDFC Bank',
          createdAt: '2025-01-21T14:15:00'
        },
        {
          templateId: 3,
          senderHeader: 'ICICIB',
          pattern: 'Card\\s+(\\d{4})\\s+debited\\s+Rs\\.(\\d+\\.?\\d*)',
          status: 'VERIFIED',
          smsType: 'DEBIT',
          paymentType: 'CARD',
          bankId: 3,
          bankName: 'ICICI Bank',
          createdAt: '2025-01-15T09:20:00'
        },
        {
          templateId: 4,
          senderHeader: 'SBIN',
          pattern: 'Rs\\.(\\d+\\.?\\d*)\\s+paid\\s+via\\s+UPI\\s+Ref\\s+(\\w+)',
          status: 'DRAFT',
          smsType: 'DEBIT',
          paymentType: 'UPI',
          bankId: 4,
          bankName: 'State Bank of India',
          createdAt: '2025-01-22T16:45:00'
        },
        {
          templateId: 5,
          senderHeader: 'AXISBK',
          pattern: 'INR\\s+(\\d+\\.?\\d*)\\s+received\\s+from\\s+(\\w+)',
          status: 'VERIFIED',
          smsType: 'CREDIT',
          paymentType: 'UPI',
          bankId: 1,
          bankName: 'Axis Bank',
          createdAt: '2025-01-18T11:00:00'
        },
        {
          templateId: 6,
          senderHeader: 'HDFCBK',
          pattern: 'Net\\s+banking\\s+payment\\s+of\\s+Rs\\.(\\d+\\.?\\d*)\\s+to\\s+(\\w+)',
          status: 'PENDING',
          smsType: 'DEBIT',
          paymentType: 'NET_BANKING',
          bankId: 2,
          bankName: 'HDFC Bank',
          createdAt: '2025-01-23T13:30:00'
        },
        {
          templateId: 7,
          senderHeader: 'ICICIB',
          pattern: 'Your\\s+A/c\\s+(\\d+)\\s+credited\\s+Rs\\.(\\d+\\.?\\d*)',
          status: 'DRAFT',
          smsType: 'CREDIT',
          paymentType: 'UPI',
          bankId: 3,
          bankName: 'ICICI Bank',
          createdAt: '2025-01-24T15:20:00'
        },
        {
          templateId: 8,
          senderHeader: 'SBIN',
          pattern: 'Card\\s+(\\d{4})\\s+used\\s+for\\s+Rs\\.(\\d+\\.?\\d*)\\s+at\\s+(\\w+)',
          status: 'VERIFIED',
          smsType: 'DEBIT',
          paymentType: 'CARD',
          bankId: 4,
          bankName: 'State Bank of India',
          createdAt: '2025-01-19T08:15:00'
        }
      ];
      setTemplates(mockTemplates);
    } catch (error) {
      console.error('Error fetching templates:', error);
      toast.error('Failed to load templates');
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
                      {(template.status === 'DRAFT' || template.status === 'PENDING') && (
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

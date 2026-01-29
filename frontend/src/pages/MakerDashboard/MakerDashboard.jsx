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
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notificationsLoading, setNotificationsLoading] = useState(true);
  const navigate = useNavigate();
  const { user } = useAuth();

  useEffect(() => {
    if (user?.userId) {
      fetchTemplates();
      fetchNotifications();
    } else {
      setLoading(false);
      setNotificationsLoading(false);
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

  const fetchNotifications = async () => {
    try {
      setNotificationsLoading(true);
      const response = await axios.get('/sms/notifications/pending');
      setNotifications(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching notifications:', error);
      if (error.response?.status !== 401) {
        toast.error('Failed to load notifications');
      }
      setNotifications([]);
    } finally {
      setNotificationsLoading(false);
    }
  };

  const handleCreateTemplateFromNotification = (notification) => {
    // Navigate to template editor with pre-filled data
    // We'll pass the notification data via state
    navigate('/maker/template/new', {
      state: {
        prefillData: {
          senderHeader: notification.senderHeader,
          sampleRawMsg: notification.smsText,
        },
        notificationId: notification.notificationId,
      },
    });
  };

  const handleResolveNotification = async (notificationId) => {
    try {
      await axios.put(`/sms/notifications/${notificationId}/resolve`);
      toast.success('Notification marked as resolved');
      fetchNotifications();
    } catch (error) {
      console.error('Error resolving notification:', error);
      toast.error('Failed to resolve notification');
    }
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
          {/* Template Request Notifications Section */}
          <div className="notifications-section">
            <h2>Template Request Notifications</h2>
            {notificationsLoading ? (
              <div className="loading-state">Loading notifications...</div>
            ) : notifications.length === 0 ? (
              <div className="empty-state">
                <p>No pending notifications.</p>
                <p className="empty-state-hint">You'll see requests here when customers submit SMS without matching templates</p>
              </div>
            ) : (
              <div className="notifications-list">
                {notifications.map((notification) => (
                  <div key={notification.notificationId} className="notification-card">
                    <div className="notification-header">
                      <div className="notification-title">
                        <h3>Sender: {notification.senderHeader}</h3>
                        <span className="notification-badge">Pending</span>
                      </div>
                      <div className="notification-actions">
                        <button
                          className="btn-create-from-notification"
                          onClick={() => handleCreateTemplateFromNotification(notification)}
                        >
                          Create Template
                        </button>
                        <button
                          className="btn-resolve-notification"
                          onClick={() => handleResolveNotification(notification.notificationId)}
                        >
                          Mark Resolved
                        </button>
                      </div>
                    </div>
                    <div className="notification-details">
                      <div className="notification-detail-item">
                        <span className="detail-label">SMS Text:</span>
                        <span className="detail-value">{notification.smsText}</span>
                      </div>
                      <div className="notification-detail-item">
                        <span className="detail-label">Requested by:</span>
                        <span className="detail-value">{notification.requestedByName}</span>
                      </div>
                      <div className="notification-detail-item">
                        <span className="detail-label">Requested on:</span>
                        <span className="detail-value">
                          {new Date(notification.createdAt).toLocaleString()}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

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

import { useState, useEffect } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import './AdminRegex.css';

function AdminRegex() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAllTemplates();
  }, []);

  const fetchAllTemplates = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/regex');
      setTemplates(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching templates:', error);
      toast.error(error.response?.status === 401 ? 'Unauthorized' : 'Failed to load templates');
      setTemplates([]);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'DRAFT': return '#ffa500';
      case 'PENDING': return '#2196F3';
      case 'VERIFIED': return '#4CAF50';
      case 'DEPRECATED': return '#f44336';
      default: return '#666';
    }
  };

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
          <p className="admin-regex-subtitle">View all templates with their status, maker, and bank</p>
          {loading ? (
            <div className="admin-regex-loading">Loading templates...</div>
          ) : templates.length === 0 ? (
            <div className="admin-regex-empty">No templates found.</div>
          ) : (
            <div className="admin-regex-table-wrapper">
              <table className="admin-regex-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Sender</th>
                    <th>Bank</th>
                    <th>SMS Type</th>
                    <th>Transaction Type</th>
                    <th>Payment Type</th>
                    <th>Sample Raw Msg</th>
                    <th>Status</th>
                    <th>Created By</th>
                    <th>Created At</th>
                  </tr>
                </thead>
                <tbody>
                  {templates.map((t) => (
                    <tr key={t.templateId}>
                      <td>{t.templateId}</td>
                      <td>{t.senderHeader}</td>
                      <td>{t.bankName ?? '—'}</td>
                      <td>{t.smsType}</td>
                      <td>{t.transactionType ?? '—'}</td>
                      <td>{t.paymentType}</td>
                      <td title={t.sampleRawMsg || ''}>
                        {t.sampleRawMsg
                          ? (t.sampleRawMsg.length > 50 ? `${t.sampleRawMsg.slice(0, 50)}…` : t.sampleRawMsg)
                          : '—'}
                      </td>
                      <td>
                        <span
                          className="admin-regex-status"
                          style={{ backgroundColor: getStatusColor(t.status) }}
                        >
                          {t.status}
                        </span>
                      </td>
                      <td>{t.createdByName ?? `ID ${t.createdById}`}</td>
                      <td>{t.createdAt ? new Date(t.createdAt).toLocaleString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default AdminRegex;

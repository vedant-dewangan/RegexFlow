import { useState, useEffect } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import './CheckerDashboard.css';

// Component to render a verification result row
function VerificationRow({ label, field }) {
  if (!field) {
    return (
      <tr>
        <td>{label}</td>
        <td>-1</td>
        <td>-</td>
      </tr>
    );
  }

  const index = field.index !== undefined ? field.index : -1;
  const value = field.value || null;
  const hasValue = index !== -1 && value !== null && value !== undefined;

  return (
    <tr>
      <td>{label}</td>
      <td className={index === -1 ? 'index-empty' : 'index-found'}>{index}</td>
      <td className={hasValue ? 'value-found' : 'value-empty'}>
        {hasValue ? value : '-'}
      </td>
    </tr>
  );
}

function CheckerDashboard() {
  const [pendingTemplates, setPendingTemplates] = useState([]);
  const [verifiedTemplates, setVerifiedTemplates] = useState([]);
  const [loadingPending, setLoadingPending] = useState(true);
  const [loadingVerified, setLoadingVerified] = useState(true);
  const [activeTab, setActiveTab] = useState('pending');
  const [testingTemplate, setTestingTemplate] = useState(null);
  const [verificationResult, setVerificationResult] = useState(null);
  const [testing, setTesting] = useState(false);

  useEffect(() => {
    fetchPendingTemplates();
    fetchVerifiedTemplates();
  }, []);

  const fetchPendingTemplates = async () => {
    try {
      setLoadingPending(true);
      const response = await axios.get('/checker/pending');
      setPendingTemplates(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching pending templates:', error);
      toast.error(error.response?.status === 401 ? 'Unauthorized' : 'Failed to load pending templates');
      setPendingTemplates([]);
    } finally {
      setLoadingPending(false);
    }
  };

  const fetchVerifiedTemplates = async () => {
    try {
      setLoadingVerified(true);
      const response = await axios.get('/checker/verified');
      setVerifiedTemplates(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching verified templates:', error);
      toast.error(error.response?.status === 401 ? 'Unauthorized' : 'Failed to load verified templates');
      setVerifiedTemplates([]);
    } finally {
      setLoadingVerified(false);
    }
  };

  const handleApprove = async (templateId) => {
    try {
      await axios.put(`/checker/approve/${templateId}`);
      toast.success('Template approved successfully!');
      // Refresh both lists
      fetchPendingTemplates();
      fetchVerifiedTemplates();
    } catch (error) {
      console.error('Error approving template:', error);
      const errorMessage = error.response?.data?.error || error.response?.data?.message || 'Failed to approve template';
      toast.error(errorMessage);
    }
  };

  const handleReject = async (templateId) => {
    if (!window.confirm('Are you sure you want to reject this template? It will be sent back to DRAFT status.')) {
      return;
    }
    
    try {
      await axios.put(`/checker/reject/${templateId}`);
      toast.success('Template rejected successfully!');
      // Refresh pending list (rejected template will be removed from pending)
      fetchPendingTemplates();
    } catch (error) {
      console.error('Error rejecting template:', error);
      const errorMessage = error.response?.data?.error || error.response?.data?.message || 'Failed to reject template';
      toast.error(errorMessage);
    }
  };

  const [testMessage, setTestMessage] = useState('');

  const openTestModal = (template) => {
    setTestingTemplate(template);
    setTestMessage(template.sampleRawMsg || '');
    setVerificationResult(null);
  };

  const closeModal = () => {
    setTestingTemplate(null);
    setVerificationResult(null);
    setTestMessage('');
  };

  const handleTestRegex = async () => {
    if (!testingTemplate?.pattern?.trim()) {
      toast.error('Template pattern is missing');
      return;
    }

    if (!testMessage?.trim()) {
      toast.error('Please enter a test message');
      return;
    }

    try {
      setTesting(true);
      setVerificationResult(null);
      
      const requestPayload = {
        regexPattern: testingTemplate.pattern,
        rawMsg: testMessage.trim(),
        smsType: testingTemplate.smsType,
        paymentType: testingTemplate.paymentType,
      };
      
      // Only include optional fields if they have values
      if (testingTemplate.transactionType) {
        requestPayload.transactionType = testingTemplate.transactionType;
      }
      if (testingTemplate.bankName) {
        requestPayload.bankName = testingTemplate.bankName;
      }
      // bankAddress is not available in template DTO, so we don't include it
      
      const response = await axios.post('/regex/process', requestPayload);
      
      setVerificationResult(response.data);
      toast.success('Regex test completed');
    } catch (error) {
      console.error('Error testing regex:', error);
      toast.error(error.response?.data?.error || error.response?.data?.message || 'Failed to test regex');
      setVerificationResult(null);
    } finally {
      setTesting(false);
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

  const formatDate = (dateString) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="checker-dashboard-container">
      <Navbar />
      <div className="checker-dashboard-header">
        <h1>Checker Dashboard</h1>
        <p>Review and approve regex templates</p>
      </div>
      <div className="checker-dashboard-content">
        {/* Tab Navigation */}
        <div className="checker-tabs">
          <button
            className={`checker-tab ${activeTab === 'pending' ? 'active' : ''}`}
            onClick={() => setActiveTab('pending')}
          >
            Pending Templates ({pendingTemplates.length})
          </button>
          <button
            className={`checker-tab ${activeTab === 'verified' ? 'active' : ''}`}
            onClick={() => setActiveTab('verified')}
          >
            Verified Templates ({verifiedTemplates.length})
          </button>
        </div>

        {/* Pending Templates Section */}
        {activeTab === 'pending' && (
          <div className="checker-dashboard-card">
            <h2>Pending Templates</h2>
            <p className="checker-subtitle">Review and approve or reject templates submitted by makers</p>
            {loadingPending ? (
              <div className="checker-loading">Loading pending templates...</div>
            ) : pendingTemplates.length === 0 ? (
              <div className="checker-empty">No pending templates found.</div>
            ) : (
              <div className="checker-templates-grid">
                {pendingTemplates.map((template) => (
                  <div key={template.templateId} className="checker-template-card">
                    <div className="checker-template-header">
                      <div className="checker-template-id">ID: {template.templateId}</div>
                      <span
                        className="checker-status-badge"
                        style={{ backgroundColor: getStatusColor(template.status) }}
                      >
                        {template.status}
                      </span>
                    </div>
                    <div className="checker-template-body">
                      <div className="checker-template-field">
                        <strong>Sender:</strong> {template.senderHeader || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Bank:</strong> {template.bankName || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>SMS Type:</strong> {template.smsType || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Transaction Type:</strong> {template.transactionType || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Payment Type:</strong> {template.paymentType || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Pattern:</strong>
                        <code className="checker-pattern">{template.pattern || '—'}</code>
                      </div>
                      <div className="checker-template-field">
                        <strong>Sample Message:</strong>
                        <div className="checker-sample-msg">{template.sampleRawMsg || '—'}</div>
                      </div>
                      <div className="checker-template-field">
                        <strong>Created By:</strong> {template.createdByName || `ID ${template.createdById}`}
                      </div>
                      <div className="checker-template-field">
                        <strong>Created At:</strong> {formatDate(template.createdAt)}
                      </div>
                    </div>
                    <div className="checker-template-actions">
                      <button
                        className="checker-btn checker-btn-test"
                        onClick={() => openTestModal(template)}
                      >
                        Test Regex
                      </button>
                      <button
                        className="checker-btn checker-btn-approve"
                        onClick={() => handleApprove(template.templateId)}
                      >
                        Approve
                      </button>
                      <button
                        className="checker-btn checker-btn-reject"
                        onClick={() => handleReject(template.templateId)}
                      >
                        Reject
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Verified Templates Section */}
        {activeTab === 'verified' && (
          <div className="checker-dashboard-card">
            <h2>Verified Templates</h2>
            <p className="checker-subtitle">View all approved templates</p>
            {loadingVerified ? (
              <div className="checker-loading">Loading verified templates...</div>
            ) : verifiedTemplates.length === 0 ? (
              <div className="checker-empty">No verified templates found.</div>
            ) : (
              <div className="checker-templates-grid">
                {verifiedTemplates.map((template) => (
                  <div key={template.templateId} className="checker-template-card">
                    <div className="checker-template-header">
                      <div className="checker-template-id">ID: {template.templateId}</div>
                      <span
                        className="checker-status-badge"
                        style={{ backgroundColor: getStatusColor(template.status) }}
                      >
                        {template.status}
                      </span>
                    </div>
                    <div className="checker-template-body">
                      <div className="checker-template-field">
                        <strong>Sender:</strong> {template.senderHeader || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Bank:</strong> {template.bankName || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>SMS Type:</strong> {template.smsType || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Transaction Type:</strong> {template.transactionType || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Payment Type:</strong> {template.paymentType || '—'}
                      </div>
                      <div className="checker-template-field">
                        <strong>Pattern:</strong>
                        <code className="checker-pattern">{template.pattern || '—'}</code>
                      </div>
                      <div className="checker-template-field">
                        <strong>Sample Message:</strong>
                        <div className="checker-sample-msg">{template.sampleRawMsg || '—'}</div>
                      </div>
                      <div className="checker-template-field">
                        <strong>Created By:</strong> {template.createdByName || `ID ${template.createdById}`}
                      </div>
                      <div className="checker-template-field">
                        <strong>Created At:</strong> {formatDate(template.createdAt)}
                      </div>
                    </div>
                    <div className="checker-template-actions">
                      <button
                        className="checker-btn checker-btn-test"
                        onClick={() => openTestModal(template)}
                      >
                        Test Regex
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Test Regex Modal */}
      {testingTemplate && (
        <div className="checker-modal-overlay" onClick={closeModal}>
          <div className="checker-modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="checker-modal-header">
              <h2>Test Regex Pattern</h2>
              <button className="checker-modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="checker-modal-body">
              <div className="checker-test-info">
                <div className="checker-test-field">
                  <strong>Template ID:</strong> {testingTemplate.templateId}
                </div>
                <div className="checker-test-field">
                  <strong>Pattern:</strong>
                  <code className="checker-pattern-display">{testingTemplate.pattern}</code>
                </div>
              </div>
              
              <div className="checker-test-input-group">
                <label htmlFor="testMessage" className="checker-test-label">
                  Test Message
                </label>
                <textarea
                  id="testMessage"
                  className="checker-test-textarea"
                  value={testMessage}
                  onChange={(e) => setTestMessage(e.target.value)}
                  placeholder="Enter a message to test against the regex pattern..."
                  rows="4"
                />
              </div>

              <div className="checker-test-actions">
                <button
                  className="checker-btn checker-btn-test-submit"
                  onClick={handleTestRegex}
                  disabled={testing || !testMessage?.trim()}
                >
                  {testing ? 'Testing...' : 'Test Regex'}
                </button>
                <button
                  className="checker-btn checker-btn-cancel"
                  onClick={closeModal}
                >
                  Close
                </button>
              </div>

              {verificationResult && (
                <div className="checker-verification-result">
                  <h3>Extracted Fields</h3>
                  
                  {/* Bank A/C Details */}
                  <div className="checker-verification-section">
                    <h4>Bank A/C Details</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="Bank A/C Id" field={verificationResult.bankAcId} />
                          <VerificationRow label="Amount" field={verificationResult.amount} />
                          <VerificationRow label="Amount (Negative)" field={verificationResult.amountNegative} />
                          <VerificationRow label="Date" field={verificationResult.date} />
                          <VerificationRow label="Merchant" field={verificationResult.merchant} />
                          <VerificationRow label="Txn Note" field={verificationResult.txnNote} />
                          <VerificationRow label="Balance" field={verificationResult.balance} />
                          <VerificationRow label="Balance (Negative)" field={verificationResult.balanceNegative} />
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Sender/Receiver Details */}
                  <div className="checker-verification-section">
                    <h4>Sender/Receiver Details</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="Sender Name" field={verificationResult.senderName} />
                          <VerificationRow label="S Bank" field={verificationResult.sBank} />
                          <VerificationRow label="S A/C Type" field={verificationResult.sAcType} />
                          <VerificationRow label="S A/C Id" field={verificationResult.sAcId} />
                          <VerificationRow label="Receiver Name" field={verificationResult.receiverName} />
                          <VerificationRow label="R Bank" field={verificationResult.rBank} />
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* General Information */}
                  <div className="checker-verification-section">
                    <h4>General Information</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="Avail Limit" field={verificationResult.availLimit} />
                          <VerificationRow label="Credit Limit" field={verificationResult.creditLimit} />
                          <VerificationRow label="Payment Type" field={verificationResult.paymentType} />
                          <VerificationRow label="City" field={verificationResult.city} />
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Biller Details */}
                  <div className="checker-verification-section">
                    <h4>Biller Details</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="Biller A/C Id" field={verificationResult.billerAcId} />
                          <VerificationRow label="Bill Id" field={verificationResult.billId} />
                          <VerificationRow label="Bill Date" field={verificationResult.billDate} />
                          <VerificationRow label="Bill Period" field={verificationResult.billPeriod} />
                          <VerificationRow label="Due Date" field={verificationResult.dueDate} />
                          <VerificationRow label="Min Amt Due" field={verificationResult.minAmtDue} />
                          <VerificationRow label="Tot Amt Due" field={verificationResult.totAmtDue} />
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* FD Details */}
                  <div className="checker-verification-section">
                    <h4>FD Details</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="Principal Amount" field={verificationResult.principalAmount} />
                          <VerificationRow label="Frequency" field={verificationResult.frequency} />
                          <VerificationRow label="Maturity Date" field={verificationResult.maturityDate} />
                          <VerificationRow label="Maturity Amount" field={verificationResult.maturityAmount} />
                          <VerificationRow label="Rate Of Interest" field={verificationResult.rateOfInterest} />
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* MF Details */}
                  <div className="checker-verification-section">
                    <h4>MF Details</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="MF Nav" field={verificationResult.mfNav} />
                          <VerificationRow label="MF Units" field={verificationResult.mfUnits} />
                          <VerificationRow label="MF ARN" field={verificationResult.mfArn} />
                          <VerificationRow label="MF Bal Units" field={verificationResult.mfBalUnits} />
                          <VerificationRow label="MF Scheme Bal" field={verificationResult.mfSchemeBal} />
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Order Details */}
                  <div className="checker-verification-section">
                    <h4>Order Details</h4>
                    <div className="checker-verification-table-container">
                      <table className="checker-verification-table">
                        <thead>
                          <tr>
                            <th>Field</th>
                            <th>Index</th>
                            <th>Value</th>
                          </tr>
                        </thead>
                        <tbody>
                          <VerificationRow label="Amount Paid" field={verificationResult.amountPaid} />
                          <VerificationRow label="Offer Amount" field={verificationResult.offerAmount} />
                          <VerificationRow label="Min Purchase Amt" field={verificationResult.minPurchaseAmt} />
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      <Footer />
    </div>
  );
}

export default CheckerDashboard;

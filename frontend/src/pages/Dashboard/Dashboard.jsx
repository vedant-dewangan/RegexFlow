import { useState, useEffect } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ExtractedFieldsCard from '../../components/ExtractedFieldsCard/ExtractedFieldsCard';
import axios from 'axios';
import toast from 'react-hot-toast';
import './Dashboard.css';

function Dashboard() {
  const [smsText, setSmsText] = useState('');
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [lastSubmission, setLastSubmission] = useState(null);

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      setFetching(true);
      const response = await axios.get('/sms/history');
      const data = response.data;
      setTransactions(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching transactions:', error);
      if (error.response?.status !== 401) {
        toast.error('Failed to load transactions');
      }
      setTransactions([]);
    } finally {
      setFetching(false);
    }
  };

  const handleAddTransaction = async () => {
    if (!smsText.trim()) {
      toast.error('Please enter an SMS message');
      return;
    }

    const smsTextToAdd = smsText.trim();
    
    try {
      setLoading(true);
      
      // Make API call to submit SMS
      const response = await axios.post('/sms/submit', {
        smsText: smsTextToAdd
      });
      
      const submissionResponse = response.data;
      
      // Store the last submission for displaying extracted fields or no-template message
      setLastSubmission(submissionResponse);
      
      // Refresh transactions list
      await fetchTransactions();
      
      // Show appropriate message
      if (submissionResponse.hasMatch) {
        toast.success('SMS processed successfully! Template matched.');
      } else {
        toast.info(submissionResponse.message || 'No template matched. Maker has been notified.');
      }
      
      // Clear input
      setSmsText('');
    } catch (error) {
      console.error('Error submitting SMS:', error);
      if (error.response?.status === 401) {
        toast.error('Please log in again');
      } else {
        toast.error(error.response?.data?.error || 'Failed to submit SMS');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleAddTransaction();
    }
  };

  return (
    <div className="dashboard-container">
      <Navbar />
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <p>Manage your SMS transactions</p>
      </div>
      <div className="dashboard-content">
        <div className="dashboard-card">
          <h2>Add SMS Transaction</h2>
          <div className="sms-input-section">
            <textarea
              className="sms-input"
              placeholder="Enter SMS message here..."
              value={smsText}
              onChange={(e) => setSmsText(e.target.value)}
              onKeyPress={handleKeyPress}
              rows={4}
            />
            <button
              className="btn-add-transaction"
              onClick={handleAddTransaction}
              disabled={loading || !smsText.trim()}
            >
              {loading ? 'Processing...' : 'Submit'}
            </button>
          </div>
          
          {/* Display extracted fields if match found */}
          {lastSubmission && lastSubmission.hasMatch && lastSubmission.extractedFields && (
            <ExtractedFieldsCard extractedFields={lastSubmission.extractedFields} />
          )}
          
          {/* Display no-template message if no match */}
          {lastSubmission && !lastSubmission.hasMatch && (
            <div className="no-template-message">
              <div className="no-template-icon">⚠️</div>
              <p className="no-template-text">{lastSubmission.message || 'No available template'}</p>
              <p className="no-template-hint">The maker has been notified to create a template for this sender.</p>
            </div>
          )}
        </div>

        <div className="dashboard-card">
          <h2>Transactions</h2>
          {fetching ? (
            <div className="loading-state">Loading transactions...</div>
          ) : !Array.isArray(transactions) || transactions.length === 0 ? (
            <div className="empty-state">
              <p>No transactions found.</p>
              <p className="empty-state-hint">Add your first SMS transaction using the form above</p>
            </div>
          ) : (
            <div className="transactions-list">
              {Array.isArray(transactions) && transactions.map((transaction) => {
                // Show only transactions with extracted fields (matched templates)
                if (!transaction.hasMatch || !transaction.extractedFields) {
                  return null;
                }
                
                return (
                  <div key={transaction.smsId || Date.now()}>
                    <ExtractedFieldsCard extractedFields={transaction.extractedFields} />
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default Dashboard;

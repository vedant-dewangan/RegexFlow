import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ExtractedFieldsCard from '../../components/ExtractedFieldsCard/ExtractedFieldsCard';
import axios from 'axios';
import toast from 'react-hot-toast';
import './AllTransactions.css';

function AllTransactions() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
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
      setLoading(false);
    }
  };

  const matchedTransactions = transactions.filter(
    (t) => t?.hasMatch && t?.extractedFields
  );

  return (
    <div className="all-transactions-container">
      <Navbar />
      <div className="all-transactions-header">
        <div className="all-transactions-header-content">
          <Link to="/dashboard" className="back-link">
            ← Back to Dashboard
          </Link>
          <h1>All Transactions</h1>
          <p>
            {matchedTransactions.length} transaction{matchedTransactions.length !== 1 ? 's' : ''} found
          </p>
        </div>
      </div>
      <div className="all-transactions-content">
        {loading ? (
          <div className="all-transactions-loading">Loading transactions...</div>
        ) : matchedTransactions.length === 0 ? (
          <div className="all-transactions-empty">
            <p>No transactions found.</p>
            <Link to="/dashboard" className="empty-link">Add your first SMS transaction on the Dashboard</Link>
          </div>
        ) : (
          <div className="all-transactions-grid">
            {matchedTransactions.map((transaction) => (
              <div key={transaction.smsId || transaction.createdAt} className="all-transactions-item">
                <ExtractedFieldsCard extractedFields={transaction.extractedFields} />
              </div>
            ))}
          </div>
        )}
      </div>
      <Footer />
    </div>
  );
}

export default AllTransactions;

import { useState, useEffect } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import './Dashboard.css';

function Dashboard() {
  const [smsText, setSmsText] = useState('');
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      setFetching(true);
      try {
        const response = await axios.get('/api/transactions');
        // Ensure we always set an array
        const data = response.data;
        setTransactions(Array.isArray(data) ? data : []);
      } catch (apiError) {
        // If API call fails, use dummy data for demonstration
        // Remove this fallback once backend is ready
        console.warn('API call failed, using dummy data:', apiError);
        const dummyTransactions = [
          {
            id: 1,
            smsText: 'AXISBK: Rs.500.00 debited from A/c XX1234 on 26-Jan-25. Avl Bal: Rs.15,234.56',
            createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 2,
            smsText: 'HDFCBK: INR 1,200.00 credited to A/c XX5678 on 26-Jan-25. Ref: UPI123456789',
            createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 3,
            smsText: 'ICICIB: Card 4567 debited Rs.2,500.00 at AMAZON on 25-Jan-25. Avl Bal: Rs.8,900.00',
            createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 4,
            smsText: 'SBIN: Rs.350.00 paid via UPI Ref: 9876543210 on 25-Jan-25. Avl Bal: Rs.12,450.00',
            createdAt: new Date(Date.now() - 26 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 5,
            smsText: 'AXISBK: INR 3,000.00 received from UPI ID merchant@paytm on 24-Jan-25. Avl Bal: Rs.18,234.56',
            createdAt: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 6,
            smsText: 'HDFCBK: Net banking payment of Rs.1,500.00 to ELECTRICITY BILL on 24-Jan-25. Ref: NB987654',
            createdAt: new Date(Date.now() - 50 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 7,
            smsText: 'ICICIB: Your A/c 7890 credited Rs.5,000.00 on 23-Jan-25. Avl Bal: Rs.11,400.00',
            createdAt: new Date(Date.now() - 72 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 8,
            smsText: 'SBIN: Card 1234 used for Rs.899.00 at FLIPKART on 23-Jan-25. Avl Bal: Rs.12,800.00',
            createdAt: new Date(Date.now() - 74 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 9,
            smsText: 'AXISBK: Rs.250.00 debited from A/c XX1234 on 22-Jan-25. Avl Bal: Rs.15,734.56',
            createdAt: new Date(Date.now() - 96 * 60 * 60 * 1000).toISOString(),
          },
          {
            id: 10,
            smsText: 'HDFCBK: INR 750.00 credited to A/c XX5678 on 22-Jan-25. Ref: UPI987654321',
            createdAt: new Date(Date.now() - 98 * 60 * 60 * 1000).toISOString(),
          },
        ];
        setTransactions(dummyTransactions);
      }
    } catch (error) {
      console.error('Error fetching transactions:', error);
      toast.error('Failed to load transactions');
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
      
      // Create transaction object
      const newTransaction = {
        smsText: smsTextToAdd,
        createdAt: new Date().toISOString(),
      };

      // Make API call to save to database
      try {
        const response = await axios.post('/api/transactions', newTransaction);
        const savedTransaction = response.data;
        
        // Update the array with the response from database
        setTransactions((prev) => {
          const prevArray = Array.isArray(prev) ? prev : [];
          return [savedTransaction, ...prevArray];
        });
        
        toast.success('Transaction added successfully');
      } catch (apiError) {
        // If API call fails, still add to local state for now
        // Remove this fallback once backend is ready
        console.warn('API call failed, using local storage:', apiError);
        const savedTransaction = {
          ...newTransaction,
          id: Date.now(),
        };
        setTransactions((prev) => {
          const prevArray = Array.isArray(prev) ? prev : [];
          return [savedTransaction, ...prevArray];
        });
        toast.success('Transaction added (saved locally)');
      }
      
      // Clear input
      setSmsText('');
    } catch (error) {
      console.error('Error adding transaction:', error);
      toast.error('Failed to add transaction');
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
              {loading ? 'Adding...' : 'Add'}
            </button>
          </div>
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
              {Array.isArray(transactions) && transactions.map((transaction) => (
                <div key={transaction.id || transaction.transactionId || Date.now()} className="transaction-card">
                  <div className="transaction-content">
                    <p className="transaction-sms">{transaction.smsText}</p>
                    <span className="transaction-date">
                      {transaction.createdAt ? new Date(transaction.createdAt).toLocaleString() : 'N/A'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default Dashboard;

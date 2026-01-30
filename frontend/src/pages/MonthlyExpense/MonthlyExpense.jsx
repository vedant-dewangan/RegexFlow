import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import ExtractedFieldsCard from '../../components/ExtractedFieldsCard/ExtractedFieldsCard';
import axios from 'axios';
import toast from 'react-hot-toast';
import { groupTransactionsByMonth, formatMonthLabel } from '../../utils/transactionUtils';
import './MonthlyExpense.css';

const FILTER_CARDS = [
  { id: 'all', label: 'All Transactions', icon: '📋', filter: () => true },
  { id: 'food', label: 'Food', icon: '🍔', filter: (t) => t._category === 'FOOD' },
  { id: 'entertainment', label: 'Entertainment', icon: '🎬', filter: (t) => t._category === 'ENTERTAINMENT' },
  { id: 'shopping', label: 'Shopping', icon: '🛒', filter: (t) => t._category === 'SHOPPING' },
  { id: 'upi', label: 'UPI', icon: '📱', filter: (t) => t._paymentMode === 'UPI' },
  { id: 'debit', label: 'All Debit', icon: '↓', filter: (t) => ['DEBIT', 'LOAN', 'SERVICE'].includes(t._type) },
  { id: 'credit', label: 'All Credit', icon: '↑', filter: (t) => t._type === 'CREDIT' },
  { id: 'card', label: 'Card Payments', icon: '💳', filter: (t) => t._paymentMode === 'CARD' },
  { id: 'bills', label: 'Bills', icon: '📄', filter: (t) => t._category === 'BILLS_UTILITIES' },
];

function MonthlyExpense() {
  const [searchParams] = useSearchParams();
  const monthParam = searchParams.get('month');

  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedMonthKey, setSelectedMonthKey] = useState(monthParam || null);
  const [activeFilter, setActiveFilter] = useState('all');

  useEffect(() => {
    fetchTransactions();
  }, []);

  useEffect(() => {
    if (monthParam) setSelectedMonthKey(monthParam);
  }, [monthParam]);

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

  const monthlyData = groupTransactionsByMonth(transactions);

  const currentMonthKey = (() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  })();

  const effectiveMonthKey = selectedMonthKey || (monthlyData.length > 0 ? monthlyData[0].monthKey : currentMonthKey);
  const selectedMonthData = monthlyData.find((m) => m.monthKey === effectiveMonthKey);

  const filteredTransactions = selectedMonthData
    ? (() => {
        const filterDef = FILTER_CARDS.find((f) => f.id === activeFilter);
        return (filterDef?.filter ? selectedMonthData.allTransactions.filter(filterDef.filter) : selectedMonthData.allTransactions) || [];
      })()
    : [];

  return (
    <div className="monthly-expense-container">
      <Navbar />
      <div className="monthly-expense-header">
        <div className="monthly-expense-header-content">
          <Link to="/dashboard" className="back-link">
            ← Back to Dashboard
          </Link>
          <h1>Monthly Expense</h1>
          <p>Summary and filters for this month&apos;s transactions</p>
        </div>
      </div>

      <div className="monthly-expense-content">
        {loading ? (
          <div className="monthly-expense-loading">Loading...</div>
        ) : monthlyData.length === 0 ? (
          <div className="monthly-expense-empty">
            <p>No transaction data yet.</p>
            <p className="empty-hint">Add SMS transactions on the Dashboard to see monthly expense summary.</p>
            <Link to="/dashboard" className="empty-link">Go to Dashboard</Link>
          </div>
        ) : (
          <>
            {/* Month selector */}
            <div className="month-selector-card">
              <h3>Select Month</h3>
              <div className="month-selector-buttons">
                {monthlyData.map((m) => (
                  <button
                    key={m.monthKey}
                    type="button"
                    className={`month-btn ${effectiveMonthKey === m.monthKey ? 'active' : ''}`}
                    onClick={() => setSelectedMonthKey(m.monthKey)}
                  >
                    {m.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Summary card */}
            <div className="summary-card">
              <h2>Summary — {formatMonthLabel(effectiveMonthKey)}</h2>
              <div className="summary-grid">
                <div className="summary-box credited">
                  <span className="summary-label">Total Credited</span>
                  <span className="summary-amount">₹{selectedMonthData?.totalCredited.toLocaleString('en-IN', { maximumFractionDigits: 2 }) ?? '0.00'}</span>
                  <span className="summary-count">{selectedMonthData?.creditedList.length ?? 0} transaction(s)</span>
                </div>
                <div className="summary-box debited">
                  <span className="summary-label">Total Debited</span>
                  <span className="summary-amount">₹{selectedMonthData?.totalDebited.toLocaleString('en-IN', { maximumFractionDigits: 2 }) ?? '0.00'}</span>
                  <span className="summary-count">{selectedMonthData?.debitedList.length ?? 0} transaction(s)</span>
                </div>
                <div className="summary-box neutral">
                  <span className="summary-label">Net</span>
                  <span className="summary-amount">
                    {selectedMonthData
                      ? `₹${(selectedMonthData.totalCredited - selectedMonthData.totalDebited).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`
                      : '₹0.00'}
                  </span>
                </div>
              </div>
            </div>

            {/* Filter cards */}
            <div className="filter-cards-section">
              <h3>Filter by</h3>
              <div className="filter-cards">
                {FILTER_CARDS.map((card) => (
                  <button
                    key={card.id}
                    type="button"
                    className={`filter-card ${activeFilter === card.id ? 'active' : ''}`}
                    onClick={() => setActiveFilter(card.id)}
                  >
                    <span className="filter-card-icon">{card.icon}</span>
                    <span className="filter-card-label">{card.label}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Filtered transaction list */}
            <div className="transactions-section">
              <h3>
                {FILTER_CARDS.find((f) => f.id === activeFilter)?.label ?? 'All'} — {filteredTransactions.length} transaction(s)
              </h3>
              {filteredTransactions.length === 0 ? (
                <div className="no-transactions-message">No transactions match this filter for the selected month.</div>
              ) : (
                <div className="monthly-expense-transactions-grid">
                  {filteredTransactions.map((t) => (
                    <div key={t.smsId || t.createdAt} className="monthly-expense-txn-item">
                      <ExtractedFieldsCard extractedFields={t.extractedFields} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>
        )}
      </div>
      <Footer />
    </div>
  );
}

export default MonthlyExpense;

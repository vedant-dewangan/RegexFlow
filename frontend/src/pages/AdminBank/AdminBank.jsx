import { useState, useEffect } from 'react';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import './AdminBank.css';

function AdminBank() {
  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    address: '',
  });

  useEffect(() => {
    fetchBanks();
  }, []);

  const fetchBanks = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/bank', {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      setBanks(response.data);
    } catch (error) {
      console.error('Error fetching banks:', error);
      toast.error(error.response?.data?.message || 'Failed to fetch banks');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.name.trim() || !formData.address.trim()) {
      toast.error('Please fill in all fields');
      return;
    }

    try {
      setIsCreating(true);
      const response = await axios.post(
        'http://localhost:8080/bank/create',
        {
          name: formData.name.trim(),
          address: formData.address.trim(),
          regexTemplateIds: [], // Empty array as default
        },
        {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      toast.success('Bank created successfully');
      setFormData({ name: '', address: '' });
      setShowCreateForm(false);
      // Refresh the banks list
      fetchBanks();
    } catch (error) {
      console.error('Error creating bank:', error);
      const errorMessage = error.response?.data || error.message || 'Failed to create bank';
      toast.error(typeof errorMessage === 'string' ? errorMessage : 'Failed to create bank');
    } finally {
      setIsCreating(false);
    }
  };

  const handleCancel = () => {
    setFormData({ name: '', address: '' });
    setShowCreateForm(false);
  };

  return (
    <div className="admin-bank-container">
      <Navbar />
      <div className="admin-bank-header">
        <h1>Bank Management</h1>
        <p>Add and manage banks</p>
      </div>
      <div className="admin-bank-content">
        <div className="admin-bank-card">
          <div className="bank-header-section">
            <h2>All Banks</h2>
            <button
              className="btn-add-bank"
              onClick={() => setShowCreateForm(!showCreateForm)}
            >
              {showCreateForm ? 'Cancel' : '+ Add New Bank'}
            </button>
          </div>

          {showCreateForm && (
            <div className="create-bank-form">
              <h3>Create New Bank</h3>
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="name">Bank Name *</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    placeholder="Enter bank name"
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="address">Bank Address *</label>
                  <textarea
                    id="address"
                    name="address"
                    value={formData.address}
                    onChange={handleInputChange}
                    placeholder="Enter bank address"
                    rows="3"
                    required
                  />
                </div>
                <div className="form-actions">
                  <button
                    type="button"
                    className="btn-cancel"
                    onClick={handleCancel}
                    disabled={isCreating}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-submit"
                    disabled={isCreating}
                  >
                    {isCreating ? 'Creating...' : 'Create Bank'}
                  </button>
                </div>
              </form>
            </div>
          )}

          {loading ? (
            <p className="loading-text">Loading banks...</p>
          ) : banks.length === 0 ? (
            <p className="no-data-text">No banks found. Create your first bank!</p>
          ) : (
            <div className="banks-table-container">
              <table className="banks-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Bank Name</th>
                    <th>Address</th>
                    <th>Regex Templates</th>
                  </tr>
                </thead>
                <tbody>
                  {banks.map((bank,i) => (
                    <tr key={i}>
                      <td>{bank.bid}</td>
                      <td className="bank-name">{bank.name}</td>
                      <td className="bank-address">{bank.address}</td>
                      <td>
                        {bank.regexTemplateIds && bank.regexTemplateIds.length > 0 ? (
                          <span className="template-count">
                            {bank.regexTemplateIds.length} template(s)
                          </span>
                        ) : (
                          <span className="no-templates">No templates</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
      <Footer />
    </div>
  );
}

export default AdminBank;

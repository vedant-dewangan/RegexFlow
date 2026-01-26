import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import FormInput from '../../components/FormInput/FormInput';
import axios from 'axios';
import toast from 'react-hot-toast';
import './TemplateEditor.css';

function TemplateEditor() {
  const { templateId } = useParams();
  const isEditMode = !!templateId;
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    senderHeader: '',
    pattern: '',
    smsType: 'DEBIT',
    paymentType: 'UPI',
    bankId: '',
  });

  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(isEditMode);

  useEffect(() => {
    fetchBanks();
    if (isEditMode) {
      fetchTemplate();
    }
  }, [templateId]);

  const fetchBanks = async () => {
    try {
      // TODO: Replace with actual API endpoint
      // const response = await axios.get('/api/banks');
      // setBanks(response.data);
      
      // Mock data for now
      const mockBanks = [
        { bId: 1, name: 'Axis Bank' },
        { bId: 2, name: 'HDFC Bank' },
        { bId: 3, name: 'ICICI Bank' },
        { bId: 4, name: 'State Bank of India' },
        { bId: 5, name: 'Kotak Mahindra Bank' },
        { bId: 6, name: 'Punjab National Bank' },
      ];
      setBanks(mockBanks);
    } catch (error) {
      console.error('Error fetching banks:', error);
      toast.error('Failed to load banks');
    }
  };

  const fetchTemplate = async () => {
    try {
      setFetching(true);
      // TODO: Replace with actual API endpoint
      // const response = await axios.get(`/api/templates/${templateId}`);
      // const template = response.data;
      // setFormData({
      //   senderHeader: template.senderHeader || '',
      //   pattern: template.pattern || '',
      //   smsType: template.smsType || 'DEBIT',
      //   paymentType: template.paymentType || 'UPI',
      //   bankId: template.bankId || '',
      // });
      
      // Mock data - matching the sample templates from MakerDashboard
      const mockTemplates = {
        1: {
          senderHeader: 'AXISBK',
          pattern: 'Rs\\.(\\d+\\.?\\d*)\\s+debited\\s+from\\s+A/c\\s+(\\d+)',
          smsType: 'DEBIT',
          paymentType: 'UPI',
          bankId: 1,
        },
        2: {
          senderHeader: 'HDFCBK',
          pattern: 'INR\\s+(\\d+\\.?\\d*)\\s+credited\\s+to\\s+A/c\\s+(\\d+)',
          smsType: 'CREDIT',
          paymentType: 'UPI',
          bankId: 2,
        },
        3: {
          senderHeader: 'ICICIB',
          pattern: 'Card\\s+(\\d{4})\\s+debited\\s+Rs\\.(\\d+\\.?\\d*)',
          smsType: 'DEBIT',
          paymentType: 'CARD',
          bankId: 3,
        },
        4: {
          senderHeader: 'SBIN',
          pattern: 'Rs\\.(\\d+\\.?\\d*)\\s+paid\\s+via\\s+UPI\\s+Ref\\s+(\\w+)',
          smsType: 'DEBIT',
          paymentType: 'UPI',
          bankId: 4,
        },
        5: {
          senderHeader: 'AXISBK',
          pattern: 'INR\\s+(\\d+\\.?\\d*)\\s+received\\s+from\\s+(\\w+)',
          smsType: 'CREDIT',
          paymentType: 'UPI',
          bankId: 1,
        },
        6: {
          senderHeader: 'HDFCBK',
          pattern: 'Net\\s+banking\\s+payment\\s+of\\s+Rs\\.(\\d+\\.?\\d*)\\s+to\\s+(\\w+)',
          smsType: 'DEBIT',
          paymentType: 'NET_BANKING',
          bankId: 2,
        },
        7: {
          senderHeader: 'ICICIB',
          pattern: 'Your\\s+A/c\\s+(\\d+)\\s+credited\\s+Rs\\.(\\d+\\.?\\d*)',
          smsType: 'CREDIT',
          paymentType: 'UPI',
          bankId: 3,
        },
        8: {
          senderHeader: 'SBIN',
          pattern: 'Card\\s+(\\d{4})\\s+used\\s+for\\s+Rs\\.(\\d+\\.?\\d*)\\s+at\\s+(\\w+)',
          smsType: 'DEBIT',
          paymentType: 'CARD',
          bankId: 4,
        },
      };
      
      const mockTemplate = mockTemplates[parseInt(templateId)];
      if (mockTemplate) {
        setFormData({
          ...mockTemplate,
          bankId: String(mockTemplate.bankId), // Convert to string for select element
        });
      } else {
        toast.error('Template not found');
        navigate('/maker/dashboard');
      }
    } catch (error) {
      console.error('Error fetching template:', error);
      toast.error('Failed to load template');
      navigate('/maker/dashboard');
    } finally {
      setFetching(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.senderHeader || !formData.pattern || !formData.bankId) {
      toast.error('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      
      if (isEditMode) {
        // TODO: Replace with actual API endpoint
        // await axios.put(`/api/templates/${templateId}`, formData);
        toast.success('Template updated successfully');
      } else {
        // TODO: Replace with actual API endpoint
        // await axios.post('/api/templates', formData);
        toast.success('Template created successfully');
      }
      
      navigate('/maker/dashboard');
    } catch (error) {
      console.error('Error saving template:', error);
      toast.error(isEditMode ? 'Failed to update template' : 'Failed to create template');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/maker/dashboard');
  };

  if (fetching) {
    return (
      <div className="template-editor-container">
        <Navbar />
        <div className="template-editor-wrapper">
          <div className="loading-state">Loading template...</div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <div className="template-editor-container">
      <Navbar />
      <div className="template-editor-wrapper">
        <div className="template-editor-header">
          <h1>{isEditMode ? 'Edit Template' : 'Create New Template'}</h1>
          <p>{isEditMode ? 'Update your regex template' : 'Create a new regex template for SMS parsing'}</p>
        </div>

        <form className="template-editor-form" onSubmit={handleSubmit}>
          <div className="form-section">
            <h2>Template Details</h2>
            
            <FormInput
              label="Sender Header"
              type="text"
              id="senderHeader"
              name="senderHeader"
              placeholder="e.g., AXISBK, HDFCBK"
              value={formData.senderHeader}
              onChange={handleChange}
              required
            />

            <div className="form-input-group">
              <label htmlFor="pattern" className="form-input-label">
                Regex Pattern
                <span className="required-asterisk">*</span>
              </label>
              <textarea
                id="pattern"
                name="pattern"
                placeholder="e.g., Rs\\.(\\d+\\.?\\d*)"
                value={formData.pattern}
                onChange={handleChange}
                required
                className="form-textarea"
                rows="4"
              />
              <small className="form-hint">
                Enter the regex pattern to match SMS messages
              </small>
            </div>

            <div className="form-input-group">
              <label htmlFor="bankId" className="form-input-label">
                Bank
                <span className="required-asterisk">*</span>
              </label>
              <select
                id="bankId"
                name="bankId"
                value={formData.bankId}
                onChange={handleChange}
                required
                className="form-select"
              >
                <option value="">Select a bank</option>
                {banks.map((bank) => (
                  <option key={bank.bId} value={bank.bId}>
                    {bank.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-row">
              <div className="form-input-group">
                <label htmlFor="smsType" className="form-input-label">
                  SMS Type
                  <span className="required-asterisk">*</span>
                </label>
                <select
                  id="smsType"
                  name="smsType"
                  value={formData.smsType}
                  onChange={handleChange}
                  required
                  className="form-select"
                >
                  <option value="DEBIT">DEBIT</option>
                  <option value="CREDIT">CREDIT</option>
                </select>
              </div>

              <div className="form-input-group">
                <label htmlFor="paymentType" className="form-input-label">
                  Payment Type
                  <span className="required-asterisk">*</span>
                </label>
                <select
                  id="paymentType"
                  name="paymentType"
                  value={formData.paymentType}
                  onChange={handleChange}
                  required
                  className="form-select"
                >
                  <option value="UPI">UPI</option>
                  <option value="CARD">CARD</option>
                  <option value="NET_BANKING">NET_BANKING</option>
                </select>
              </div>
            </div>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={handleCancel}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={loading}
            >
              {loading ? 'Saving...' : isEditMode ? 'Update Template' : 'Create Template'}
            </button>
          </div>
        </form>
      </div>
      <Footer />
    </div>
  );
}

export default TemplateEditor;

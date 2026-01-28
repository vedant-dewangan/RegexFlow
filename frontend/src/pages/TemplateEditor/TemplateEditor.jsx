import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import axios from 'axios';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import './TemplateEditor.css';

const PAYMENT_TYPES = ['UPI', 'NET_BANKING', 'CREDIT_CARD', 'DEBIT_CARD', 'CASH', 'CHEQUE'];
const TRANSACTION_TYPES = [
  'UPI_CREDIT', 'UPI_DEBIT', 'ATM_WITHDRAWAL', 'CASH_DEPOSIT',
  'ELECTRICITY_BILL', 'MOBILE_RECHARGE', 'EMI_DEBIT', 'LOAN_CREDIT',
  'CREDIT_CARD_PAYMENT', 'DEBIT_CARD_SPEND', 'MUTUAL_FUND_PURCHASE', 'FIXED_DEPOSIT_MATURITY',
];

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

function TemplateEditor() {
  const { templateId } = useParams();
  const { user } = useAuth();
  const isEditMode = !!templateId && templateId !== 'new';
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    senderHeader: '',
    bankId: '',
    bankName: '',
    bankAddress: '',
    smsType: 'DEBIT',
    paymentType: 'UPI',
    transactionType: 'UPI_DEBIT',
    pattern: '',
    sampleRawMsg: '',
  });

  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(isEditMode);
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);
  const [currentTemplateId, setCurrentTemplateId] = useState(null);

  const getBankId = (bank) => bank.bId ?? bank.bid;

  useEffect(() => {
    fetchBanks();
    if (isEditMode && user?.userId) {
      fetchTemplate();
    } else if (isEditMode && !user?.userId) {
      setFetching(false);
    }
  }, [templateId, user?.userId]);

  const fetchBanks = async () => {
    try {
      const response = await axios.get('/bank');
      setBanks(response.data);
    } catch (error) {
      console.error('Error fetching banks:', error);
      toast.error('Failed to load banks');
    }
  };

  const fetchTemplate = async () => {
    if (!user?.userId) return;
    try {
      setFetching(true);
      const response = await axios.get(`/regex/${user.userId}`);
      const templates = response.data;
      const template = templates.find((t) => String(t.templateId) === String(templateId));
      if (!template) {
        toast.error('Template not found');
        navigate('/maker/dashboard');
        return;
      }
      setCurrentTemplateId(template.templateId);
      const bankAddr = banks.length
        ? (banks.find((x) => String(getBankId(x)) === String(template.bankId))?.address || '')
        : '';
      setFormData({
        senderHeader: template.senderHeader || '',
        bankId: String(template.bankId || ''),
        bankName: template.bankName || '',
        bankAddress: bankAddr,
        smsType: template.smsType || 'DEBIT',
        paymentType: template.paymentType || 'UPI',
        transactionType: template.transactionType || 'UPI_DEBIT',
        pattern: template.pattern || '',
        sampleRawMsg: template.sampleRawMsg || '',
      });
    } catch (error) {
      console.error('Error fetching template:', error);
      toast.error(error.response?.data?.message || 'Failed to load template');
      navigate('/maker/dashboard');
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    if (banks.length && formData.bankId && !formData.bankAddress) {
      const b = banks.find((x) => String(getBankId(x)) === formData.bankId);
      if (b?.address) setFormData((prev) => ({ ...prev, bankAddress: b.address }));
    }
  }, [banks, formData.bankId, formData.bankAddress]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    // If bank is selected, auto-populate bank name and address
    if (name === 'bankId') {
      const selectedBank = banks.find(bank => String(getBankId(bank)) === value);
      if (selectedBank) {
        setFormData((prev) => ({
          ...prev,
          bankId: value,
          bankName: selectedBank.name,
          bankAddress: selectedBank.address || '',
        }));
        return;
      }
    }
    
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleVerify = async () => {
    if (!formData.pattern?.trim() || !formData.sampleRawMsg?.trim()) {
      toast.error('Please enter both regex pattern and sample raw message');
      return;
    }
    try {
      setVerifying(true);
      setVerificationResult(null);
      const response = await axios.post('/regex/process', {
        regexPattern: formData.pattern,
        rawMsg: formData.sampleRawMsg.trim(),
        smsType: formData.smsType,
        paymentType: formData.paymentType,
        transactionType: formData.transactionType || null,
        bankName: formData.bankName || null,
        bankAddress: formData.bankAddress || null,
      });
      setVerificationResult(response.data);
      toast.success('Regex verification completed');
    } catch (error) {
      console.error('Error verifying regex:', error);
      toast.error(error.response?.data?.error || error.response?.data?.message || 'Failed to verify regex');
      setVerificationResult(null);
    } finally {
      setVerifying(false);
    }
  };

  const buildDraftPayload = () => ({
    senderHeader: formData.senderHeader.trim(),
    pattern: formData.pattern.trim(),
    sampleRawMsg: formData.sampleRawMsg?.trim() || null,
    smsType: formData.smsType,
    transactionType: formData.transactionType,
    bankId: Number(formData.bankId),
    paymentType: formData.paymentType,
  });

  const handleSaveDraft = async () => {
    if (!formData.senderHeader?.trim() || !formData.pattern?.trim() || !formData.bankId) {
      toast.error('Please fill sender header, pattern, and bank');
      return;
    }
    try {
      setLoading(true);
      const payload = buildDraftPayload();
      const response = await axios.post('/regex/save-as-draft', payload);
      toast.success('Draft saved successfully');
      setCurrentTemplateId(response.data.templateId);
      if (!isEditMode) navigate(`/maker/template/${response.data.templateId}`, { replace: true });
    } catch (error) {
      const msg = error.response?.data?.error || error.response?.data?.message || 'Failed to save draft';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitForApproval = async () => {
    const id = currentTemplateId || templateId;
    if (!id) {
      toast.error('Save as draft first, then submit for approval');
      return;
    }
    if (!formData.senderHeader?.trim() || !formData.pattern?.trim() || !formData.bankId) {
      toast.error('Please fill sender header, pattern, and bank');
      return;
    }
    try {
      setLoading(true);
      const payload = buildDraftPayload();
      await axios.put(`/regex/push/${id}`, payload);
      toast.success('Submitted for approval');
      navigate('/maker/dashboard');
    } catch (error) {
      const msg = error.response?.data?.error || error.response?.data?.message || 'Failed to submit';
      toast.error(msg);
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

        <form className="template-editor-form" onSubmit={(e) => e.preventDefault()}>
          <div className="form-section">
            <h2>Template Details</h2>

            <div className="form-input-group">
              <label htmlFor="senderHeader" className="form-input-label">
                Sender Header
                <span className="required-asterisk">*</span>
              </label>
              <input
                id="senderHeader"
                name="senderHeader"
                type="text"
                placeholder="e.g. AXISBK, HDFCBK"
                value={formData.senderHeader}
                onChange={handleChange}
                required
                className="form-input"
              />
              <small className="form-hint">SMS sender identifier (e.g. bank short code)</small>
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
                {banks.map((bank) => {
                  const bankId = getBankId(bank);
                  return (
                    <option key={bankId} value={bankId}>
                      {bank.name}
                    </option>
                  );
                })}
              </select>
            </div>

            <div className="form-input-group">
              <label htmlFor="bankAddress" className="form-input-label">
                Bank Address
              </label>
              <textarea
                id="bankAddress"
                name="bankAddress"
                placeholder="Auto-filled when you select a bank"
                value={formData.bankAddress}
                onChange={handleChange}
                className="form-textarea"
                rows="2"
                readOnly
              />
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
                  {PAYMENT_TYPES.map((pt) => (
                    <option key={pt} value={pt}>{pt}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-input-group">
              <label htmlFor="transactionType" className="form-input-label">
                Transaction Type
                <span className="required-asterisk">*</span>
              </label>
              <select
                id="transactionType"
                name="transactionType"
                value={formData.transactionType}
                onChange={handleChange}
                required
                className="form-select"
              >
                {TRANSACTION_TYPES.map((tt) => (
                  <option key={tt} value={tt}>{tt}</option>
                ))}
              </select>
            </div>

            <div className="form-input-group">
              <label htmlFor="pattern" className="form-input-label">
                Regex Pattern
                <span className="required-asterisk">*</span>
              </label>
              <textarea
                id="pattern"
                name="pattern"
                placeholder="e.g. Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)"
                value={formData.pattern}
                onChange={handleChange}
                required
                className="form-textarea"
                rows="4"
              />
              <small className="form-hint">
                Use named groups e.g. (?&lt;amount&gt;\\d+) for extraction
              </small>
            </div>

            <div className="form-input-group">
              <label htmlFor="sampleRawMsg" className="form-input-label">
                Sample Raw Message
              </label>
              <textarea
                id="sampleRawMsg"
                name="sampleRawMsg"
                placeholder="e.g. Rs.500.00 debited from A/c XX1234 on 01-Jan-25. Avl Bal: Rs.10000.00"
                value={formData.sampleRawMsg}
                onChange={handleChange}
                className="form-textarea"
                rows="4"
              />
              <small className="form-hint">
                Sample SMS message stored with the template. Use Verify to test the regex pattern against this message.
              </small>
            </div>
          </div>

          {verificationResult && (
            <div className="verification-result">
              <h3>Verification Results</h3>
              
              {/* Bank A/C Details */}
              <div className="verification-section">
                <h4>Bank A/C Details</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="Bank A/C Id" 
                        field={verificationResult.bankAcId} 
                      />
                      <VerificationRow 
                        label="Amount" 
                        field={verificationResult.amount} 
                      />
                      <VerificationRow 
                        label="Amount (Negative)" 
                        field={verificationResult.amountNegative} 
                      />
                      <VerificationRow 
                        label="Date" 
                        field={verificationResult.date} 
                      />
                      <VerificationRow 
                        label="Merchant" 
                        field={verificationResult.merchant} 
                      />
                      <VerificationRow 
                        label="Txn Note" 
                        field={verificationResult.txnNote} 
                      />
                      <VerificationRow 
                        label="Balance" 
                        field={verificationResult.balance} 
                      />
                      <VerificationRow 
                        label="Balance (Negative)" 
                        field={verificationResult.balanceNegative} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Sender/Receiver Details */}
              <div className="verification-section">
                <h4>Sender/Receiver Details</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="Sender Name" 
                        field={verificationResult.senderName} 
                      />
                      <VerificationRow 
                        label="S Bank" 
                        field={verificationResult.sBank} 
                      />
                      <VerificationRow 
                        label="S A/C Type" 
                        field={verificationResult.sAcType} 
                      />
                      <VerificationRow 
                        label="S A/C Id" 
                        field={verificationResult.sAcId} 
                      />
                      <VerificationRow 
                        label="Receiver Name" 
                        field={verificationResult.receiverName} 
                      />
                      <VerificationRow 
                        label="R Bank" 
                        field={verificationResult.rBank} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>

              {/* General Information */}
              <div className="verification-section">
                <h4>General Information</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="Avail Limit" 
                        field={verificationResult.availLimit} 
                      />
                      <VerificationRow 
                        label="Credit Limit" 
                        field={verificationResult.creditLimit} 
                      />
                      <VerificationRow 
                        label="Payment Type" 
                        field={verificationResult.paymentType} 
                      />
                      <VerificationRow 
                        label="City" 
                        field={verificationResult.city} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Biller Details */}
              <div className="verification-section">
                <h4>Biller Details</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="Biller A/C Id" 
                        field={verificationResult.billerAcId} 
                      />
                      <VerificationRow 
                        label="Bill Id" 
                        field={verificationResult.billId} 
                      />
                      <VerificationRow 
                        label="Bill Date" 
                        field={verificationResult.billDate} 
                      />
                      <VerificationRow 
                        label="Bill Period" 
                        field={verificationResult.billPeriod} 
                      />
                      <VerificationRow 
                        label="Due Date" 
                        field={verificationResult.dueDate} 
                      />
                      <VerificationRow 
                        label="Min Amt Due" 
                        field={verificationResult.minAmtDue} 
                      />
                      <VerificationRow 
                        label="Tot Amt Due" 
                        field={verificationResult.totAmtDue} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>

              {/* FD Details */}
              <div className="verification-section">
                <h4>FD Details</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="Principal Amount" 
                        field={verificationResult.principalAmount} 
                      />
                      <VerificationRow 
                        label="Frequency" 
                        field={verificationResult.frequency} 
                      />
                      <VerificationRow 
                        label="Maturity Date" 
                        field={verificationResult.maturityDate} 
                      />
                      <VerificationRow 
                        label="Maturity Amount" 
                        field={verificationResult.maturityAmount} 
                      />
                      <VerificationRow 
                        label="Rate Of Interest" 
                        field={verificationResult.rateOfInterest} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>

              {/* MF Details */}
              <div className="verification-section">
                <h4>MF Details</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="MF Nav" 
                        field={verificationResult.mfNav} 
                      />
                      <VerificationRow 
                        label="MF Units" 
                        field={verificationResult.mfUnits} 
                      />
                      <VerificationRow 
                        label="MF ARN" 
                        field={verificationResult.mfArn} 
                      />
                      <VerificationRow 
                        label="MF Bal Units" 
                        field={verificationResult.mfBalUnits} 
                      />
                      <VerificationRow 
                        label="MF Scheme Bal" 
                        field={verificationResult.mfSchemeBal} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Order Details */}
              <div className="verification-section">
                <h4>Order Details</h4>
                <div className="verification-table-container">
                  <table className="verification-table">
                    <thead>
                      <tr>
                        <th>Field</th>
                        <th>Index</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <VerificationRow 
                        label="Amount Paid" 
                        field={verificationResult.amountPaid} 
                      />
                      <VerificationRow 
                        label="Offer Amount" 
                        field={verificationResult.offerAmount} 
                      />
                      <VerificationRow 
                        label="Min Purchase Amt" 
                        field={verificationResult.minPurchaseAmt} 
                      />
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}

          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={handleCancel}
              disabled={loading || verifying}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn-verify"
              onClick={handleVerify}
              disabled={loading || verifying}
            >
              {verifying ? 'Verifying...' : 'Verify'}
            </button>
            <button
              type="button"
              className="btn-draft"
              onClick={handleSaveDraft}
              disabled={loading || verifying}
            >
              Save Draft
            </button>
            <button
              type="button"
              className="btn-submit"
              onClick={handleSubmitForApproval}
              disabled={loading || verifying}
            >
              Submit for Approval
            </button>
          </div>
        </form>
      </div>
      <Footer />
    </div>
  );
}

export default TemplateEditor;

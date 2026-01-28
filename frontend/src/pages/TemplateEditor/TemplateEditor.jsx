import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Navbar from '../../components/Navbar/Navbar';
import Footer from '../../components/Footer/Footer';
import FormInput from '../../components/FormInput/FormInput';
import axios from 'axios';
import toast from 'react-hot-toast';
import './TemplateEditor.css';

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
  const isEditMode = !!templateId;
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    bankId: '',
    bankName: '',
    bankAddress: '',
    smsType: 'DEBIT',
    paymentType: 'UPI',
    transactionType: '',
    regexPattern: '',
    rawMsg: '',
  });

  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(isEditMode);
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);

  // Helper function to get bank ID (handles both bId and bid)
  const getBankId = (bank) => {
    return bank.bId || bank.bid;
  };

  useEffect(() => {
    fetchBanks();
    if (isEditMode) {
      fetchTemplate();
    }
  }, [templateId]);

  const fetchBanks = async () => {
    try {
      const response = await axios.get('http://localhost:8080/bank', {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
        },
      });
      setBanks(response.data);
    } catch (error) {
      console.error('Error fetching banks:', error);
      toast.error('Failed to load banks');
    }
  };

  const fetchTemplate = async () => {
    try {
      setFetching(true);
      // TODO: Replace with actual API endpoint when available
      toast.error('Template editing not yet implemented');
      navigate('/maker/dashboard');
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
    if (!formData.regexPattern || !formData.rawMsg) {
      toast.error('Please enter both regex pattern and raw message');
      return;
    }

    if (!formData.bankId || !formData.bankAddress) {
      toast.error('Please select a bank');
      return;
    }

    try {
      setVerifying(true);
      setVerificationResult(null);

      const response = await axios.post(
        'http://localhost:8080/regex/process',
        {
          bankAddress: formData.bankAddress,
          smsType: formData.smsType,
          paymentType: formData.paymentType,
          regexPattern: formData.regexPattern,
          rawMsg: formData.rawMsg,
          bankName: formData.bankName,
          transactionType: formData.transactionType || null,
        },
        {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );

      setVerificationResult(response.data);
      toast.success('Regex verification completed');
    } catch (error) {
      console.error('Error verifying regex:', error);
      toast.error(error.response?.data?.message || 'Failed to verify regex pattern');
      setVerificationResult(null);
    } finally {
      setVerifying(false);
    }
  };

  const handleSaveDraft = () => {
    // TODO: Implement save draft functionality
    toast.info('Save Draft functionality will be implemented soon');
  };

  const handleSubmitForApproval = () => {
    // TODO: Implement submit for approval functionality
    toast.info('Submit for Approval functionality will be implemented soon');
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
              <label htmlFor="bankId" className="form-input-label">
                Bank Name
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
                placeholder="Bank address will be auto-populated when you select a bank"
                value={formData.bankAddress}
                onChange={handleChange}
                className="form-textarea"
                rows="3"
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
                  <option value="UPI">UPI</option>
                  <option value="NET_BANKING">NET_BANKING</option>
                  <option value="CHEQUE_PAYMENT">CHEQUE_PAYMENT</option>
                  <option value="CASH_DEPOSIT">CASH_DEPOSIT</option>
                  <option value="CASH_WITHDRAWAL">CASH_WITHDRAWAL</option>
                  <option value="CREDIT_CARD">CREDIT_CARD</option>
                  <option value="DEBIT_CARD">DEBIT_CARD</option>
                </select>
              </div>
            </div>

            <div className="form-input-group">
              <label htmlFor="transactionType" className="form-input-label">
                Transaction Type
              </label>
              <select
                id="transactionType"
                name="transactionType"
                value={formData.transactionType}
                onChange={handleChange}
                className="form-select"
              >
                <option value="">Select transaction type (optional)</option>
                <option value="EMI_INSTALLMENT">EMI_INSTALLMENT</option>
                <option value="LOAN_REPAYMENT">LOAN_REPAYMENT</option>
                <option value="CREDIT_CARD_BILL_PAYMENT">CREDIT_CARD_BILL_PAYMENT</option>
              </select>
            </div>

            <div className="form-input-group">
              <label htmlFor="regexPattern" className="form-input-label">
                Regex Pattern
                <span className="required-asterisk">*</span>
              </label>
              <textarea
                id="regexPattern"
                name="regexPattern"
                placeholder="e.g., Rs\\.(\\d+\\.?\\d*)\\s+debited\\s+from\\s+A/c\\s+(\\d+)"
                value={formData.regexPattern}
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
              <label htmlFor="rawMsg" className="form-input-label">
                Raw Message
                <span className="required-asterisk">*</span>
              </label>
              <textarea
                id="rawMsg"
                name="rawMsg"
                placeholder="Enter a sample SMS message to test the regex pattern"
                value={formData.rawMsg}
                onChange={handleChange}
                required
                className="form-textarea"
                rows="4"
              />
              <small className="form-hint">
                Enter a sample SMS message to verify the regex pattern
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

import './ExtractedFieldsCard.css';

function ExtractedFieldsCard({ extractedFields }) {
  if (!extractedFields || !extractedFields.fields || Object.keys(extractedFields.fields).length === 0) {
    return null;
  }

  const { fields, amount, date, merchant, balance, transactionType, smsType } = extractedFields;

  // Determine transaction type from fields - prioritize smsType from template
  const determineTransactionType = () => {
    // First priority: Use smsType from template (most reliable)
    if (smsType === 'DEBIT' || smsType === 'CREDIT' || smsType === 'LOAN' || smsType === 'SERVICE') {
      return smsType;
    }
    
    // Second priority: Use transactionType if explicitly set
    if (transactionType === 'DEBIT' || transactionType === 'CREDIT' || transactionType === 'LOAN' || transactionType === 'SERVICE') {
      return transactionType;
    }
    
    // Third priority: Check for amountNegative field (indicates debit)
    if (fields.amountNegative) {
      return 'DEBIT';
    }
    
    // Fourth priority: Check SMS text patterns in fields or look for keywords
    const smsText = fields.smsText || '';
    const lowerText = smsText.toLowerCase();
    
    // Common loan keywords
    if (lowerText.includes('loan') || 
        lowerText.includes('emi') ||
        lowerText.includes('installment') ||
        lowerText.includes('repayment') ||
        lowerText.includes('disbursed') ||
        lowerText.includes('sanctioned')) {
      return 'LOAN';
    }
    
    // Common service keywords
    if (lowerText.includes('service') || 
        lowerText.includes('bill') ||
        lowerText.includes('recharge') ||
        lowerText.includes('subscription') ||
        lowerText.includes('payment due') ||
        lowerText.includes('invoice')) {
      return 'SERVICE';
    }
    
    // Common debit keywords
    if (lowerText.includes('debited') || 
        lowerText.includes('withdrawn') || 
        lowerText.includes('spent') ||
        lowerText.includes('paid') ||
        lowerText.includes('deducted')) {
      return 'DEBIT';
    }
    
    // Common credit keywords
    if (lowerText.includes('credited') || 
        lowerText.includes('received') || 
        lowerText.includes('deposited') ||
        lowerText.includes('added')) {
      return 'CREDIT';
    }
    
    // Default to null if cannot determine
    return null;
  };

  const transactionTypeResult = determineTransactionType();
  const isDebit = transactionTypeResult === 'DEBIT';
  const isCredit = transactionTypeResult === 'CREDIT';
  const isLoan = transactionTypeResult === 'LOAN';
  const isService = transactionTypeResult === 'SERVICE';

  // Get card class based on transaction type
  const getCardClass = () => {
    if (isDebit) return 'debit-card';
    if (isCredit) return 'credit-card';
    if (isLoan) return 'loan-card';
    if (isService) return 'service-card';
    return 'default-card';
  };

  // Get type icon and text
  const getTypeInfo = () => {
    if (isDebit) return { icon: '↓', text: 'Debited' };
    if (isCredit) return { icon: '↑', text: 'Credited' };
    if (isLoan) return { icon: '💰', text: 'Loan' };
    if (isService) return { icon: '🔧', text: 'Service' };
    return { icon: '•', text: 'Transaction' };
  };

  // Get amount symbol
  const getAmountSymbol = () => {
    if (isDebit) return '-';
    if (isCredit) return '+';
    if (isLoan) return '-'; // Loan repayments are typically debits
    if (isService) return '-'; // Service payments are typically debits
    return '';
  };

  const typeInfo = getTypeInfo();

  // Format field name from camelCase to Title Case
  const formatFieldName = (fieldName) => {
    // Handle special cases
    const specialCases = {
      'bankAcId': 'Bank A/C ID',
      'sBank': 'Sender Bank',
      'rBank': 'Receiver Bank',
      'sAcType': 'Sender A/C Type',
      'sAcId': 'Sender A/C ID',
      'txnNote': 'Transaction Note',
      'paymentType': 'Payment Mode',
      'availLimit': 'Available Limit',
      'creditLimit': 'Credit Limit',
      'billerAcId': 'Biller A/C ID',
      'billId': 'Bill ID',
      'billDate': 'Bill Date',
      'billPeriod': 'Bill Period',
      'dueDate': 'Due Date',
      'minAmtDue': 'Min Amount Due',
      'totAmtDue': 'Total Amount Due',
      'principalAmount': 'Principal Amount',
      'maturityDate': 'Maturity Date',
      'maturityAmount': 'Maturity Amount',
      'rateOfInterest': 'Rate of Interest',
      'mfNav': 'MF NAV',
      'mfUnits': 'MF Units',
      'mfArn': 'MF ARN',
      'mfBalUnits': 'MF Balance Units',
      'mfSchemeBal': 'MF Scheme Balance',
      'amountPaid': 'Amount Paid',
      'offerAmount': 'Offer Amount',
      'minPurchaseAmt': 'Min Purchase Amount',
      'amountNegative': 'Amount Negative',
      'balanceNegative': 'Balance Negative',
      'smsText': 'SMS Text'
    };

    if (specialCases[fieldName]) {
      return specialCases[fieldName];
    }

    // Convert camelCase to Title Case
    return fieldName
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  };

  // Get all extracted fields to display
  const getFieldsToDisplay = () => {
    const fieldsToDisplay = [];
    const alreadyShown = new Set(['amount', 'date', 'merchant', 'balance']); // These are shown separately
    
    // Add all fields from the fields object
    Object.keys(fields).forEach(key => {
      const value = fields[key];
      // Only include fields that have values and aren't already shown
      if (value && value.trim() !== '' && !alreadyShown.has(key)) {
        fieldsToDisplay.push({
          key,
          label: formatFieldName(key),
          value: value
        });
      }
    });

    // Sort fields for consistent display (common fields first)
    const fieldOrder = [
      'paymentType', 'txnNote', 'bankAcId', 'senderName', 'receiverName',
      'sBank', 'rBank', 'sAcType', 'sAcId', 'city', 'availLimit', 'creditLimit',
      'billerAcId', 'billId', 'billDate', 'billPeriod', 'dueDate', 'minAmtDue', 'totAmtDue',
      'principalAmount', 'frequency', 'maturityDate', 'maturityAmount', 'rateOfInterest',
      'mfNav', 'mfUnits', 'mfArn', 'mfBalUnits', 'mfSchemeBal',
      'amountPaid', 'offerAmount', 'minPurchaseAmt'
    ];

    fieldsToDisplay.sort((a, b) => {
      const indexA = fieldOrder.indexOf(a.key);
      const indexB = fieldOrder.indexOf(b.key);
      if (indexA === -1 && indexB === -1) return a.label.localeCompare(b.label);
      if (indexA === -1) return 1;
      if (indexB === -1) return -1;
      return indexA - indexB;
    });

    return fieldsToDisplay;
  };

  const fieldsToDisplay = getFieldsToDisplay();

  return (
    <div className={`transaction-summary-card ${getCardClass()}`}>
      <div className="transaction-type-indicator">
        <span className="type-icon">{typeInfo.icon}</span>
        <span className="type-text">{typeInfo.text}</span>
      </div>
      
      <div className="transaction-amount">
        <span className="amount-symbol">{getAmountSymbol()}</span>
        <span className="amount-value">₹{amount || '0.00'}</span>
      </div>

      <div className="transaction-details">
        {date && (
          <div className="detail-row">
            <span className="detail-label">Date:</span>
            <span className="detail-value">{date}</span>
          </div>
        )}
        
        {merchant && (
          <div className="detail-row">
            <span className="detail-label">Merchant:</span>
            <span className="detail-value">{merchant}</span>
          </div>
        )}

        {balance && (
          <div className="detail-row">
            <span className="detail-label">Balance:</span>
            <span className="detail-value">₹{balance}</span>
          </div>
        )}

        {/* Display all other extracted fields */}
        {fieldsToDisplay.map((field) => (
          <div key={field.key} className="detail-row">
            <span className="detail-label">{field.label}:</span>
            <span className="detail-value">{field.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ExtractedFieldsCard;

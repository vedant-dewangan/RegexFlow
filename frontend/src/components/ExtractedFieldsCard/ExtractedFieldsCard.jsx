import './ExtractedFieldsCard.css';

function ExtractedFieldsCard({ extractedFields }) {
  if (!extractedFields || !extractedFields.fields || Object.keys(extractedFields.fields).length === 0) {
    return null;
  }

  const { fields, amount, date, merchant, balance, transactionType, smsType } = extractedFields;

  // Determine transaction type from fields - prioritize smsType from template
  const determineTransactionType = () => {
    // First priority: Use smsType from template (most reliable)
    if (smsType === 'DEBIT' || smsType === 'CREDIT') {
      return smsType;
    }
    
    // Second priority: Use transactionType if explicitly set
    if (transactionType === 'DEBIT' || transactionType === 'CREDIT') {
      return transactionType;
    }
    
    // Third priority: Check for amountNegative field (indicates debit)
    if (fields.amountNegative) {
      return 'DEBIT';
    }
    
    // Fourth priority: Check SMS text patterns in fields or look for keywords
    const smsText = fields.smsText || '';
    const lowerText = smsText.toLowerCase();
    
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

  // Get payment mode (UPI, Card, etc.)
  const paymentMode = fields.paymentType || fields.txnNote || 'N/A';

  return (
    <div className={`transaction-summary-card ${!isDebit ? 'debit-card' : !isCredit ? 'credit-card' : 'default-card'}`}>
      <div className="transaction-type-indicator">
        <span className="type-icon">{isDebit ? '↓' : isCredit ? '↑' : '•'}</span>
        <span className="type-text">{isDebit ? 'Debited' : isCredit ? 'Credited' : 'Transaction'}</span>
      </div>
      
      <div className="transaction-amount">
        <span className="amount-symbol">{isDebit ? '-' : isCredit ? '+' : ''}</span>
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
        
        <div className="detail-row">
          <span className="detail-label">Mode:</span>
          <span className="detail-value">{paymentMode}</span>
        </div>

        {balance && (
          <div className="detail-row">
            <span className="detail-label">Balance:</span>
            <span className="detail-value">₹{balance}</span>
          </div>
        )}
      </div>
    </div>
  );
}

export default ExtractedFieldsCard;

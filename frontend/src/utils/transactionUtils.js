/**
 * Transaction utilities: parse amount, infer type (debit/credit), category, payment mode, and month.
 */

const CATEGORY_KEYWORDS = {
  FOOD: [
    'swiggy', 'zomato', 'dominos', 'pizza', 'mcdonald', 'burger', 'restaurant', 'cafe', 'café',
    'dining', 'food', 'grocery', 'bigbasket', 'blinkit', 'instamart', 'dunzo', 'eats'
  ],
  TRANSPORT: [
    'uber', 'ola', 'rapido', 'petrol', 'fuel', 'indianoil', 'hp petrol', 'shell', 'metro',
    'irctc', 'railway', 'bus', 'parking', 'toll', 'fastag'
  ],
  ENTERTAINMENT: [
    'netflix', 'prime', 'amazon prime', 'hotstar', 'disney', 'spotify', 'youtube', 'movie',
    'cinema', 'pvr', 'inox', 'gaming', 'steam', 'playstation', 'xbox'
  ],
  SHOPPING: [
    'amazon', 'flipkart', 'myntra', 'ajio', 'meesho', 'shopping', 'mall'
  ],
  BILLS_UTILITIES: [
    'electricity', 'water', 'gas', 'broadband', 'wifi', 'jio', 'airtel', 'vodafone', 'recharge',
    'bill', 'utility', 'bsnl', 'act fiber'
  ],
  LOAN_EMI: [
    'loan', 'emi', 'repayment', 'disbursed', 'sanctioned', 'installment', 'bajaj', 'hdfc loan',
    'icici loan', 'sbi loan', 'personal loan', 'home loan', 'car loan'
  ],
  SUBSCRIPTION: [
    'subscription', 'renewal', 'annual', 'monthly plan', 'membership'
  ],
  TRANSFER: [
    'transfer', 'imps', 'neft', 'rtgs', 'to account', 'self transfer', 'received from', 'sent to'
  ],
  HEALTH: [
    'pharmacy', 'apollo', 'medplus', '1mg', 'pharmeasy', 'hospital', 'doctor', 'clinic', 'health'
  ],
  TRAVEL: [
    'booking', 'makemytrip', 'goibibo', 'airline', 'flight', 'hotel', 'travel', 'visa'
  ],
  INVESTMENT: [
    'mutual fund', 'sip', 'zerodha', 'groww', 'investment', 'stock', 'demat', 'ppf', 'fd', 'fixed deposit'
  ]
};

/**
 * Determine transaction type (DEBIT, CREDIT, LOAN, SERVICE) from extracted fields.
 */
export function getTransactionType(extractedFields) {
  if (!extractedFields) return null;
  const { smsType, transactionType, fields } = extractedFields;
  if (smsType === 'DEBIT' || smsType === 'CREDIT' || smsType === 'LOAN' || smsType === 'SERVICE') return smsType;
  if (transactionType === 'DEBIT' || transactionType === 'CREDIT' || transactionType === 'LOAN' || transactionType === 'SERVICE') return transactionType;
  if (fields?.amountNegative) return 'DEBIT';
  const text = (fields?.smsText || extractedFields?.smsText || '').toLowerCase();
  if (/loan|emi|repayment|disbursed|installment/.test(text)) return 'LOAN';
  if (/service|bill|recharge|subscription|invoice/.test(text)) return 'SERVICE';
  if (/debited|withdrawn|spent|paid|deducted/.test(text)) return 'DEBIT';
  if (/credited|received|deposited|added/.test(text)) return 'CREDIT';
  return null;
}

/**
 * Parse amount string to number (handles "1,234.56" and "1234").
 */
export function parseAmount(amountStr) {
  if (amountStr == null || amountStr === '') return 0;
  const cleaned = String(amountStr).replace(/,/g, '').replace(/[^\d.-]/g, '');
  const num = parseFloat(cleaned);
  return isNaN(num) ? 0 : num;
}

/**
 * Infer category from merchant and SMS text.
 */
export function inferCategory(extractedFields) {
  const merchant = (extractedFields?.merchant || '').toLowerCase();
  const text = (extractedFields?.fields?.smsText || extractedFields?.smsText || '').toLowerCase();
  const combined = `${merchant} ${text}`;

  for (const [category, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
    if (keywords.some(kw => combined.includes(kw))) return category;
  }
  return 'OTHER';
}

/**
 * Get payment mode (UPI, Card, etc.) from fields.
 */
export function getPaymentMode(extractedFields) {
  const fields = extractedFields?.fields || {};
  const paymentType = (fields.paymentType || fields.txnNote || extractedFields?.transactionType || '').toUpperCase();
  if (paymentType.includes('UPI') || paymentType.includes('BHARATPE') || paymentType.includes('PHONEPE') || paymentType.includes('GPAY')) return 'UPI';
  if (paymentType.includes('CREDIT') || paymentType.includes('CARD')) return 'CARD';
  if (paymentType.includes('NET') || paymentType.includes('BANKING') || paymentType.includes('NEFT') || paymentType.includes('IMPS')) return 'NET_BANKING';
  if (paymentType.includes('CASH')) return 'CASH';
  if (paymentType.includes('CHEQUE')) return 'CHEQUE';
  return 'OTHER';
}

/**
 * Get month key (YYYY-MM) and display label from createdAt or date string.
 */
export function getMonthKey(createdAt, dateStr) {
  if (createdAt) {
    const d = new Date(createdAt);
    return { key: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`, date: d };
  }
  if (dateStr) {
    const d = parseDate(dateStr);
    if (d) return { key: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`, date: d };
  }
  return { key: null, date: null };
}

function parseDate(dateStr) {
  if (!dateStr) return null;
  const d = new Date(dateStr);
  return isNaN(d.getTime()) ? null : d;
}

/**
 * Format month key to display label (e.g. "Jan 2025").
 */
export function formatMonthLabel(monthKey) {
  if (!monthKey) return '';
  const [y, m] = monthKey.split('-');
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return `${months[parseInt(m, 10) - 1]} ${y}`;
}

/**
 * Group transactions by month and compute credited/debited and category breakdowns.
 */
export function groupTransactionsByMonth(transactions) {
  const matched = (Array.isArray(transactions) ? transactions : [])
    .filter(t => t?.hasMatch && t?.extractedFields);
  const byMonth = {};

  matched.forEach(t => {
    const { key: monthKey } = getMonthKey(t.createdAt, t.extractedFields?.date);
    if (!monthKey) return;
    if (!byMonth[monthKey]) {
      byMonth[monthKey] = {
        monthKey,
        label: formatMonthLabel(monthKey),
        totalCredited: 0,
        totalDebited: 0,
        byCategory: {},
        byPaymentMode: {},
        debitedList: [],
        creditedList: [],
        upiList: [],
        cardList: [],
        allTransactions: []
      };
    }
    const amount = parseAmount(t.extractedFields?.amount);
    const type = getTransactionType(t.extractedFields);
    const category = inferCategory(t.extractedFields);
    const paymentMode = getPaymentMode(t.extractedFields);
    const entry = { ...t, _amount: amount, _type: type, _category: category, _paymentMode: paymentMode };

    byMonth[monthKey].allTransactions.push(entry);

    if (type === 'CREDIT') {
      byMonth[monthKey].totalCredited += amount;
      byMonth[monthKey].creditedList.push(entry);
    } else if (type === 'DEBIT' || type === 'LOAN' || type === 'SERVICE') {
      byMonth[monthKey].totalDebited += amount;
      byMonth[monthKey].debitedList.push(entry);
    }

    byMonth[monthKey].byCategory[category] = (byMonth[monthKey].byCategory[category] || 0) + amount;
    byMonth[monthKey].byPaymentMode[paymentMode] = (byMonth[monthKey].byPaymentMode[paymentMode] || 0) + amount;

    if (paymentMode === 'UPI') byMonth[monthKey].upiList.push(entry);
    if (paymentMode === 'CARD') byMonth[monthKey].cardList.push(entry);
  });

  return Object.values(byMonth).sort((a, b) => b.monthKey.localeCompare(a.monthKey));
}

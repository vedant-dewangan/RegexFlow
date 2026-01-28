# API Testing Samples for Regex Endpoints

## 1. GET `/regex` - Fetch All Regex Templates (Admin Only)

**Method:** GET  
**URL:** `http://localhost:8080/regex`  
**Headers:**
```
Cookie: JSESSIONID=<your-session-id>
```
**Authentication:** Admin role required  
**Request Body:** None

**Example Response:**
```json
[
  {
    "templateId": 1,
    "senderHeader": "AXISBK",
    "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
    "smsType": "DEBIT",
    "transactionType": "UPI_DEBIT",
    "status": "DRAFT",
    "createdById": 2,
    "createdByName": "John Maker",
    "createdAt": "2026-01-28T10:30:00",
    "bankId": 1,
    "bankName": "Axis Bank",
    "auditLogId": null,
    "paymentType": "UPI"
  },
  {
    "templateId": 2,
    "senderHeader": "HDFCBK",
    "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+credited\\s+to\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
    "smsType": "CREDIT",
    "transactionType": "UPI_CREDIT",
    "status": "PENDING",
    "createdById": 3,
    "createdByName": "Jane Maker",
    "createdAt": "2026-01-28T11:15:00",
    "bankId": 2,
    "bankName": "HDFC Bank",
    "auditLogId": null,
    "paymentType": "UPI"
  }
]
```

---

## 2. GET `/regex/{id}` - Fetch Templates by Maker ID

**Method:** GET  
**URL:** `http://localhost:8080/regex/2` (where 2 is the maker/user ID)  
**Headers:**
```
Cookie: JSESSIONID=<your-session-id>
```
**Authentication:** Any authenticated user  
**Request Body:** None

**Example Response:**
```json
[
  {
    "templateId": 1,
    "senderHeader": "AXISBK",
    "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
    "smsType": "DEBIT",
    "transactionType": "UPI_DEBIT",
    "status": "DRAFT",
    "createdById": 2,
    "createdByName": "John Maker",
    "createdAt": "2026-01-28T10:30:00",
    "bankId": 1,
    "bankName": "Axis Bank",
    "auditLogId": null,
    "paymentType": "UPI"
  }
]
```

---

## 3. POST `/regex/save-as-draft` - Save Template as Draft

**Method:** POST  
**URL:** `http://localhost:8080/regex/save-as-draft`  
**Headers:**
```
Content-Type: application/json
Cookie: JSESSIONID=<your-session-id>
```
**Authentication:** Any authenticated user  
**Note:** Duplicate drafts are prevented. If a draft with the same pattern, sender header, bank, SMS type, transaction type, and payment type already exists, the request will fail with a 400 error.

**Request Body:**

Use **`sampleRawMsg`** (not `rawMsg`) for the sample message; **`pattern`** (not `regexPattern`); **`bankId`** (numeric) and **`senderHeader`**. The `/regex/process` endpoint uses `rawMsg` and does not write to the database.

### Sample 1: UPI Debit Transaction
```json
{
  "senderHeader": "AXISBK",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "sampleRawMsg": "Rs.500.00 debited from A/c 123456 on 28-01-2026 at 10:30. Balance: Rs.10000.00",
  "smsType": "DEBIT",
  "transactionType": "UPI_DEBIT",
  "bankId": 1,
  "paymentType": "UPI"
}
```

### Sample 2: UPI Credit Transaction
```json
{
  "senderHeader": "HDFCBK",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+credited\\s+to\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "sampleRawMsg": "Rs.1000.00 credited to A/c XX1338 on 20/01/2026. Balance: Rs.15000.00",
  "smsType": "CREDIT",
  "transactionType": "UPI_CREDIT",
  "bankId": 2,
  "paymentType": "UPI"
}
```

### Sample 3: ATM Withdrawal
```json
{
  "senderHeader": "ICICIB",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+withdrawn\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "ATM_WITHDRAWAL",
  "bankId": 3,
  "paymentType": "CASH"
}
```

### Sample 4: Credit Card Payment
```json
{
  "senderHeader": "SBIIN",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+paid\\s+for\\s+Card\\s+(?<cardNumber>\\d{4}X{8}\\d{4})\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+.*?Available\\s+limit:\\s+Rs\\.(?<availableLimit>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "CREDIT_CARD_PAYMENT",
  "bankId": 4,
  "paymentType": "CREDIT_CARD"
}
```

### Sample 5: Mobile Recharge
```json
{
  "senderHeader": "KOTAKB",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+for\\s+Mobile\\s+Recharge\\s+to\\s+(?<mobileNumber>\\d{10})\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "MOBILE_RECHARGE",
  "bankId": 5,
  "paymentType": "NET_BANKING"
}
```

**Example Response (201 Created):**
```json
{
  "templateId": 3,
  "senderHeader": "AXISBK",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "UPI_DEBIT",
  "status": "DRAFT",
  "createdById": 2,
  "createdByName": "John Maker",
  "createdAt": "2026-01-28T14:30:00",
  "bankId": 1,
  "bankName": "Axis Bank",
  "auditLogId": null,
  "paymentType": "UPI"
}
```

---

## 4. PUT `/regex/push/{templateId}` - Update Template to PENDING Status

**Method:** PUT  
**URL:** `http://localhost:8080/regex/push/1` (where 1 is the template ID)  
**Headers:**
```
Content-Type: application/json
Cookie: JSESSIONID=<your-session-id>
```
**Authentication:** Any authenticated user  
**Note:** 
- This endpoint updates an existing DRAFT template and changes its status to PENDING
- Only the creator of the template can update it
- Only DRAFT templates can be updated to PENDING status
- The templateId in the URL must match an existing template

**Request Body:**

### Sample 1: UPI Debit Transaction
```json
{
  "senderHeader": "AXISBK",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "UPI_DEBIT",
  "bankId": 1,
  "paymentType": "UPI"
}
```

### Sample 2: EMI Debit
```json
{
  "senderHeader": "HDFCBK",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+EMI\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "EMI_DEBIT",
  "bankId": 2,
  "paymentType": "NET_BANKING"
}
```

### Sample 3: Electricity Bill Payment
```json
{
  "senderHeader": "ICICIB",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+paid\\s+for\\s+Electricity\\s+Bill\\s+Account\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "ELECTRICITY_BILL",
  "bankId": 3,
  "paymentType": "NET_BANKING"
}
```

**Example Response (200 OK):**
```json
{
  "templateId": 1,
  "senderHeader": "AXISBK",
  "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
  "smsType": "DEBIT",
  "transactionType": "UPI_DEBIT",
  "status": "PENDING",
  "createdById": 2,
  "createdByName": "John Maker",
  "createdAt": "2026-01-28T10:30:00",
  "bankId": 1,
  "bankName": "Axis Bank",
  "auditLogId": null,
  "paymentType": "UPI"
}
```

**Error Responses:**
- `404 NOT_FOUND`: Template with the given ID doesn't exist
- `400 BAD_REQUEST`: 
  - Template is not in DRAFT status (only DRAFT templates can be updated)
  - You're not the creator of the template
  - Invalid request data

---

## Field Descriptions

### Required Fields for POST Requests:
- **senderHeader**: The SMS sender identifier (e.g., "AXISBK", "HDFCBK")
- **pattern**: The regex pattern with named groups (e.g., `(?<amount>\\d+)`)
- **smsType**: Either `"DEBIT"` or `"CREDIT"`
- **transactionType**: One of the TransactionType enum values (see below)
- **bankId**: The ID of the bank (must exist in database)
- **paymentType**: One of the PaymentType enum values (see below)

### Auto-Generated Fields (Don't include in request):
- **templateId**: Auto-generated
- **createdById**: Set from session
- **createdByName**: Auto-populated from user
- **createdAt**: Auto-generated timestamp
- **status**: Set automatically (DRAFT for save-as-draft, PENDING for push)
- **auditLogId**: null initially

### Available Enum Values:

**SmsType:**
- `"DEBIT"`
- `"CREDIT"`

**TransactionType:**
- `"UPI_CREDIT"`
- `"UPI_DEBIT"`
- `"ATM_WITHDRAWAL"`
- `"CASH_DEPOSIT"`
- `"ELECTRICITY_BILL"`
- `"MOBILE_RECHARGE"`
- `"EMI_DEBIT"`
- `"LOAN_CREDIT"`
- `"CREDIT_CARD_PAYMENT"`
- `"DEBIT_CARD_SPEND"`
- `"MUTUAL_FUND_PURCHASE"`
- `"FIXED_DEPOSIT_MATURITY"`

**PaymentType:**
- `"UPI"`
- `"NET_BANKING"`
- `"CREDIT_CARD"`
- `"DEBIT_CARD"`
- `"CASH"`
- `"CHEQUE"`

---

## Testing with cURL

### 1. GET All Templates (Admin)
```bash
curl -X GET "http://localhost:8080/regex" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -b cookies.txt
```

### 2. GET Templates by Maker ID
```bash
curl -X GET "http://localhost:8080/regex/2" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -b cookies.txt
```

### 3. Save as Draft
```bash
curl -X POST "http://localhost:8080/regex/save-as-draft" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -b cookies.txt \
  -d '{
    "senderHeader": "AXISBK",
    "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
    "smsType": "DEBIT",
    "transactionType": "UPI_DEBIT",
    "bankId": 1,
    "paymentType": "UPI"
  }'
```

### 4. Push for Approval (Update Draft to PENDING)
```bash
curl -X PUT "http://localhost:8080/regex/push/1" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=your-session-id" \
  -b cookies.txt \
  -d '{
    "senderHeader": "AXISBK",
    "pattern": "Rs\\.(?<amount>\\d+(?:\\.\\d{2})?)\\s+debited\\s+from\\s+A/c\\s+(?<accountNumber>\\d+)\\s+on\\s+(?<date>\\d{2}-\\d{2}-\\d{4})\\s+at\\s+(?<time>\\d{2}:\\d{2})\\s+.*?Balance:\\s+Rs\\.(?<balance>\\d+(?:\\.\\d{2})?)",
    "smsType": "DEBIT",
    "transactionType": "UPI_DEBIT",
    "bankId": 1,
    "paymentType": "UPI"
  }'
```

---

## Notes:
1. **Authentication**: All endpoints require a valid session. Make sure to login first via `/auth/login` to get a session.
2. **Bank ID**: The `bankId` must exist in your database. Check your bank table to get valid IDs.
3. **Regex Pattern**: The pattern should include named groups like `(?<fieldName>pattern)` for field extraction.
4. **Status**: 
   - `/save-as-draft` automatically sets status to `DRAFT`
   - `/push/{templateId}` updates an existing DRAFT template and sets status to `PENDING`
5. **Created By**: The `createdById` is automatically set from the session, so you don't need to include it in the request.
6. **Duplicate Prevention**: 
   - `/save-as-draft` prevents creating duplicate drafts with the same pattern, sender header, bank, SMS type, transaction type, and payment type
   - If a duplicate is detected, you'll get a 400 BAD_REQUEST error
7. **Push Endpoint**: 
   - Only works on DRAFT templates
   - Only the creator of the template can push it for approval
   - The template must exist and be in DRAFT status

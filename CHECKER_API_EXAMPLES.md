# Checker Service API Examples

This document provides practical examples for testing the Checker Service APIs using curl or any HTTP client.

## Prerequisites

- Backend server running on `http://localhost:8080`
- Valid session cookie after login
- User with CHECKER or ADMIN role

## Authentication

First, login to get a session:

```bash
# Login as CHECKER
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "checker@example.com",
    "password": "password123"
  }' \
  -c cookies.txt

# The session cookie will be saved in cookies.txt
```

## 1. Get All Pending Templates

Retrieve all templates waiting for review:

```bash
curl -X GET http://localhost:8080/checker/pending \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
[
  {
    "templateId": 1,
    "senderHeader": "HDFCBK",
    "pattern": "Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*credited.*A/c.*(?<accountNumber>X+\\d{4})",
    "sampleRawMsg": "Rs. 5000.00 credited to A/c XX1234 on 28-Jan-26",
    "smsType": "TRANSACTIONAL",
    "transactionType": "CREDIT",
    "status": "PENDING",
    "createdById": 2,
    "createdByName": "John Maker",
    "createdAt": "2026-01-28T10:30:00",
    "bankId": 1,
    "bankName": "HDFC Bank",
    "paymentType": "NEFT",
    "auditLogId": null
  }
]
```

## 2. Get Template Details

Review a specific template before making a decision:

```bash
curl -X GET http://localhost:8080/checker/template/1 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "templateId": 1,
  "senderHeader": "HDFCBK",
  "pattern": "Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*credited.*A/c.*(?<accountNumber>X+\\d{4})",
  "sampleRawMsg": "Rs. 5000.00 credited to A/c XX1234 on 28-Jan-26",
  "smsType": "TRANSACTIONAL",
  "transactionType": "CREDIT",
  "status": "PENDING",
  "createdById": 2,
  "createdByName": "John Maker",
  "createdAt": "2026-01-28T10:30:00",
  "bankId": 1,
  "bankName": "HDFC Bank",
  "paymentType": "NEFT",
  "auditLogId": null
}
```

## 3. Approve a Template

Approve a template (changes status from PENDING to VERIFIED):

```bash
curl -X PUT http://localhost:8080/checker/approve/1 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "templateId": 1,
  "senderHeader": "HDFCBK",
  "pattern": "Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*credited.*A/c.*(?<accountNumber>X+\\d{4})",
  "sampleRawMsg": "Rs. 5000.00 credited to A/c XX1234 on 28-Jan-26",
  "smsType": "TRANSACTIONAL",
  "transactionType": "CREDIT",
  "status": "VERIFIED",
  "createdById": 2,
  "createdByName": "John Maker",
  "createdAt": "2026-01-28T10:30:00",
  "bankId": 1,
  "bankName": "HDFC Bank",
  "paymentType": "NEFT",
  "auditLogId": 5
}
```

## 4. Reject a Template

Reject a template (changes status from PENDING back to DRAFT):

```bash
curl -X PUT http://localhost:8080/checker/reject/2 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "templateId": 2,
  "senderHeader": "ICICIB",
  "pattern": "(?<amount>\\d+\\.\\d{2}).*debited",
  "sampleRawMsg": "Rs 2000.00 debited from your account",
  "smsType": "TRANSACTIONAL",
  "transactionType": "DEBIT",
  "status": "DRAFT",
  "createdById": 2,
  "createdByName": "John Maker",
  "createdAt": "2026-01-28T11:00:00",
  "bankId": 2,
  "bankName": "ICICI Bank",
  "paymentType": "UPI",
  "auditLogId": 6
}
```

## 5. Get All Verified Templates

View all approved templates:

```bash
curl -X GET http://localhost:8080/checker/verified \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
[
  {
    "templateId": 1,
    "senderHeader": "HDFCBK",
    "pattern": "Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*credited.*A/c.*(?<accountNumber>X+\\d{4})",
    "sampleRawMsg": "Rs. 5000.00 credited to A/c XX1234 on 28-Jan-26",
    "smsType": "TRANSACTIONAL",
    "transactionType": "CREDIT",
    "status": "VERIFIED",
    "createdById": 2,
    "createdByName": "John Maker",
    "createdAt": "2026-01-28T10:30:00",
    "bankId": 1,
    "bankName": "HDFC Bank",
    "paymentType": "NEFT",
    "auditLogId": 5
  }
]
```

## Error Scenarios

### 1. Unauthorized Access (Not a CHECKER)

```bash
# Login as MAKER
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maker@example.com",
    "password": "password123"
  }' \
  -c maker_cookies.txt

# Try to approve (will fail)
curl -X PUT http://localhost:8080/checker/approve/1 \
  -b maker_cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:** `401 Unauthorized`

### 2. Template Not Found

```bash
curl -X PUT http://localhost:8080/checker/approve/9999 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "error": "Template not found with id: 9999"
}
```
**Status Code:** `404 Not Found`

### 3. Invalid Status Transition

```bash
# Try to approve a template that's already VERIFIED
curl -X PUT http://localhost:8080/checker/approve/1 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "error": "Only PENDING templates can be approved. Current status: VERIFIED"
}
```
**Status Code:** `400 Bad Request`

### 4. Try to approve a DRAFT template

```bash
curl -X PUT http://localhost:8080/checker/approve/3 \
  -b cookies.txt \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "error": "Only PENDING templates can be approved. Current status: DRAFT"
}
```
**Status Code:** `400 Bad Request`

## Complete Workflow Example

Here's a complete workflow from template creation to approval:

```bash
# 1. Login as MAKER
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maker@example.com",
    "password": "password123"
  }' \
  -c maker_cookies.txt

# 2. Create a draft template
curl -X POST http://localhost:8080/regex/save-as-draft \
  -b maker_cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "senderHeader": "SBIINB",
    "pattern": "Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*debited.*A/c.*(?<accountNumber>X+\\d{4})",
    "sampleRawMsg": "Rs. 1500.00 debited from A/c XX5678 on 28-Jan-26",
    "smsType": "TRANSACTIONAL",
    "transactionType": "DEBIT",
    "bankId": 3,
    "paymentType": "IMPS"
  }'

# 3. Submit template for review (assume templateId is 10)
curl -X PUT http://localhost:8080/regex/push/10 \
  -b maker_cookies.txt \
  -H "Content-Type: application/json" \
  -d '{
    "senderHeader": "SBIINB",
    "pattern": "Rs\\.?\\s*(?<amount>[\\d,]+\\.?\\d*).*debited.*A/c.*(?<accountNumber>X+\\d{4})",
    "sampleRawMsg": "Rs. 1500.00 debited from A/c XX5678 on 28-Jan-26",
    "smsType": "TRANSACTIONAL",
    "transactionType": "DEBIT",
    "bankId": 3,
    "paymentType": "IMPS"
  }'

# 4. Logout maker
# (Session ends)

# 5. Login as CHECKER
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "checker@example.com",
    "password": "password123"
  }' \
  -c checker_cookies.txt

# 6. View pending templates
curl -X GET http://localhost:8080/checker/pending \
  -b checker_cookies.txt \
  -H "Content-Type: application/json"

# 7. Get template details for review
curl -X GET http://localhost:8080/checker/template/10 \
  -b checker_cookies.txt \
  -H "Content-Type: application/json"

# 8. Approve the template
curl -X PUT http://localhost:8080/checker/approve/10 \
  -b checker_cookies.txt \
  -H "Content-Type: application/json"

# 9. Verify it's in verified list
curl -X GET http://localhost:8080/checker/verified \
  -b checker_cookies.txt \
  -H "Content-Type: application/json"
```

## Using Postman

### Setup

1. Import the following as a Postman collection
2. Set base URL: `http://localhost:8080`
3. Use "Send and download" to save cookies automatically

### Collection Structure

```
RegexFlow - Checker Service
├── Auth
│   └── Login as Checker
├── Pending Templates
│   └── GET /checker/pending
├── Template Details
│   └── GET /checker/template/:templateId
├── Approve Template
│   └── PUT /checker/approve/:templateId
├── Reject Template
│   └── PUT /checker/reject/:templateId
└── Verified Templates
    └── GET /checker/verified
```

## Frontend Integration

If you're building a frontend, here's how to integrate:

```javascript
// Get pending templates
const getPendingTemplates = async () => {
  const response = await fetch('http://localhost:8080/checker/pending', {
    method: 'GET',
    credentials: 'include', // Important for session cookies
    headers: {
      'Content-Type': 'application/json'
    }
  });
  
  if (response.ok) {
    const templates = await response.json();
    return templates;
  } else {
    throw new Error('Failed to fetch pending templates');
  }
};

// Approve template
const approveTemplate = async (templateId) => {
  const response = await fetch(`http://localhost:8080/checker/approve/${templateId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    }
  });
  
  if (response.ok) {
    const approvedTemplate = await response.json();
    return approvedTemplate;
  } else {
    const error = await response.json();
    throw new Error(error.error || 'Failed to approve template');
  }
};

// Reject template
const rejectTemplate = async (templateId) => {
  const response = await fetch(`http://localhost:8080/checker/reject/${templateId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    }
  });
  
  if (response.ok) {
    const rejectedTemplate = await response.json();
    return rejectedTemplate;
  } else {
    const error = await response.json();
    throw new Error(error.error || 'Failed to reject template');
  }
};
```

## Notes

1. All requests require authentication via session cookies
2. Only CHECKER and ADMIN roles can access these endpoints
3. Templates must be in PENDING status to be approved or rejected
4. Each approval/rejection creates an audit log entry
5. Rejected templates return to DRAFT status for maker revision

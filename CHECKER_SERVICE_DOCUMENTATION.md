# Checker Service Documentation

## Overview

The Checker Service is responsible for managing the approval workflow of regex templates. It allows users with CHECKER or ADMIN roles to review templates that are in PENDING status and either approve or reject them.

## Architecture

### Components

1. **CheckerService** - Business logic layer
2. **CheckerController** - REST API endpoints
3. **AuditLog** - Tracks approval/rejection history
4. **RegexTemplate** - Template entity with status management

## Template Status Flow

```
DRAFT → PENDING → VERIFIED (Approved)
              ↓
            DRAFT (Rejected - returns to draft for revision)
```

### Status Descriptions

- **DRAFT**: Initial state when template is created
- **PENDING**: Template submitted by MAKER for review
- **VERIFIED**: Template approved by CHECKER
- **DEPRECATED**: Template marked as obsolete (future use)

## API Endpoints

### 1. Get Pending Templates

**Endpoint**: `GET /checker/pending`

**Access**: CHECKER, ADMIN

**Description**: Retrieves all templates with PENDING status that need review.

**Response**:
```json
[
  {
    "templateId": 1,
    "senderHeader": "HDFC",
    "pattern": "(?<amount>\\d+\\.\\d{2}).*credited",
    "sampleRawMsg": "Rs 1000.00 credited to your account",
    "smsType": "TRANSACTIONAL",
    "transactionType": "CREDIT",
    "status": "PENDING",
    "createdById": 2,
    "createdByName": "John Maker",
    "createdAt": "2026-01-28T10:30:00",
    "bankId": 1,
    "bankName": "HDFC Bank",
    "paymentType": "UPI"
  }
]
```

### 2. Get Verified Templates

**Endpoint**: `GET /checker/verified`

**Access**: CHECKER, ADMIN

**Description**: Retrieves all templates with VERIFIED status.

**Response**: Same structure as pending templates, but with `"status": "VERIFIED"`

### 3. Get Template Details

**Endpoint**: `GET /checker/template/{templateId}`

**Access**: CHECKER, ADMIN

**Description**: Retrieves detailed information about a specific template for review.

**Path Parameters**:
- `templateId` (Long) - ID of the template to retrieve

**Response**: Single template object (same structure as above)

### 4. Approve Template

**Endpoint**: `PUT /checker/approve/{templateId}`

**Access**: CHECKER, ADMIN

**Description**: Approves a template, changing its status from PENDING to VERIFIED and creating an audit log entry.

**Path Parameters**:
- `templateId` (Long) - ID of the template to approve

**Success Response** (200 OK):
```json
{
  "templateId": 1,
  "status": "VERIFIED",
  "auditLogId": 5,
  ...
}
```

**Error Responses**:
- `401 UNAUTHORIZED` - User not authenticated or doesn't have CHECKER/ADMIN role
- `404 NOT_FOUND` - Template not found
- `400 BAD_REQUEST` - Template is not in PENDING status

### 5. Reject Template

**Endpoint**: `PUT /checker/reject/{templateId}`

**Access**: CHECKER, ADMIN

**Description**: Rejects a template, changing its status from PENDING back to DRAFT (so maker can revise it) and creating an audit log entry.

**Path Parameters**:
- `templateId` (Long) - ID of the template to reject

**Success Response** (200 OK):
```json
{
  "templateId": 1,
  "status": "DRAFT",
  "auditLogId": 6,
  ...
}
```

**Error Responses**: Same as approve endpoint

## Service Methods

### CheckerService.java

#### `getPendingTemplates()`
- Returns list of all templates with PENDING status
- Used by checkers to see their review queue

#### `getVerifiedTemplates()`
- Returns list of all templates with VERIFIED status
- Used to view approved templates

#### `getTemplateById(Long templateId)`
- Returns details of a specific template
- Used for detailed review before approval/rejection

#### `approveTemplate(Long templateId, Long checkerId)`
- Changes template status from PENDING to VERIFIED
- Creates audit log with APPROVED status
- Records which checker approved it and when
- **Transactional**: Both operations succeed or fail together

#### `rejectTemplate(Long templateId, Long checkerId)`
- Changes template status from PENDING to DRAFT
- Creates audit log with REJECTED status
- Records which checker rejected it and when
- **Transactional**: Both operations succeed or fail together

## Audit Log

Every approval or rejection creates an audit log entry:

```java
AuditLog {
  auditId: Long,
  status: AuditStatus (APPROVED or REJECTED),
  verifiedBy: Users (the checker who made the decision),
  verifiedAt: LocalDateTime (timestamp),
  template: RegexTemplate (the template being reviewed)
}
```

### Audit Status Enum
- `CREATED` - Initial state (not used in checker service)
- `SUBMITTED` - Template submitted (not used in checker service)
- `APPROVED` - Template approved by checker
- `REJECTED` - Template rejected by checker

## Security & Authorization

### Role-Based Access Control

Only users with **CHECKER** or **ADMIN** roles can:
- View pending templates
- View verified templates
- Approve templates
- Reject templates

### Session Validation

All endpoints validate:
1. User is authenticated (has valid session token)
2. User has userId in session
3. User has CHECKER or ADMIN role

## Business Rules

### Approval Rules
1. Only templates in PENDING status can be approved
2. Approved templates move to VERIFIED status
3. An audit log with APPROVED status is created
4. The checker's ID and timestamp are recorded

### Rejection Rules
1. Only templates in PENDING status can be rejected
2. Rejected templates move back to DRAFT status
3. An audit log with REJECTED status is created
4. The maker can revise and resubmit the template

### Validation
- Template must exist
- Template must be in PENDING status
- Checker must be a valid user
- All operations are transactional

## Database Schema

### RegexTemplate Table
```sql
template_id (PK)
sender_header
pattern (TEXT)
sample_raw_msg (TEXT)
sms_type (ENUM)
transaction_type (ENUM)
payment_type (ENUM)
status (ENUM: DRAFT, PENDING, VERIFIED, DEPRECATED)
created_by (FK → Users)
created_at (TIMESTAMP)
bank_id (FK → Bank)
```

### AuditLog Table
```sql
audit_id (PK)
status (ENUM: CREATED, SUBMITTED, APPROVED, REJECTED)
verified_by (FK → Users)
verified_at (TIMESTAMP)
template_id (FK → RegexTemplate, UNIQUE)
```

## Usage Example

### Typical Workflow

1. **Maker creates a template** (status: DRAFT)
   ```
   POST /regex/save-as-draft
   ```

2. **Maker submits template for review** (status: PENDING)
   ```
   PUT /regex/push/{templateId}
   ```

3. **Checker views pending templates**
   ```
   GET /checker/pending
   ```

4. **Checker reviews template details**
   ```
   GET /checker/template/{templateId}
   ```

5. **Checker approves or rejects**
   ```
   PUT /checker/approve/{templateId}  // Moves to VERIFIED
   OR
   PUT /checker/reject/{templateId}   // Moves back to DRAFT
   ```

6. **If rejected, maker can revise and resubmit**
   ```
   PUT /regex/push/{templateId}  // Back to PENDING
   ```

## Error Handling

All endpoints return appropriate HTTP status codes:

- `200 OK` - Successful operation
- `401 UNAUTHORIZED` - Not authenticated or wrong role
- `404 NOT_FOUND` - Template not found
- `400 BAD_REQUEST` - Invalid operation (e.g., wrong status)
- `500 INTERNAL_SERVER_ERROR` - Server error

Error responses include a JSON message:
```json
{
  "error": "Only PENDING templates can be approved. Current status: VERIFIED"
}
```

## Testing

### Test Scenarios

1. **Approve a pending template**
   - Create template as MAKER
   - Submit to PENDING
   - Login as CHECKER
   - Approve template
   - Verify status is VERIFIED
   - Verify audit log created

2. **Reject a pending template**
   - Create template as MAKER
   - Submit to PENDING
   - Login as CHECKER
   - Reject template
   - Verify status is DRAFT
   - Verify audit log created

3. **Authorization checks**
   - Try to approve as MAKER (should fail)
   - Try to approve as CUSTOMER (should fail)
   - Try to approve as CHECKER (should succeed)
   - Try to approve as ADMIN (should succeed)

4. **Invalid status transitions**
   - Try to approve DRAFT template (should fail)
   - Try to approve VERIFIED template (should fail)
   - Try to reject DRAFT template (should fail)

## Future Enhancements

1. **Comments/Feedback**: Add reason field for rejections
2. **Bulk Operations**: Approve/reject multiple templates at once
3. **Notifications**: Notify makers when their templates are approved/rejected
4. **History**: View all audit logs for a template
5. **Reassignment**: Allow reassigning templates to different checkers
6. **SLA Tracking**: Track how long templates stay in PENDING status

## Related Files

- `CheckerService.java` - Service implementation
- `CheckerController.java` - REST endpoints
- `RegexTemplate.java` - Template entity
- `AuditLog.java` - Audit log entity
- `RegexTemplateStatus.java` - Status enum
- `AuditStatus.java` - Audit status enum
- `RegexTemplateRepository.java` - Database queries
- `AuditLogRepository.java` - Audit log queries

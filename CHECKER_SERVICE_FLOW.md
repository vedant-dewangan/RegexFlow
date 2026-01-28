# Checker Service Flow Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         RegexFlow System                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│              │         │              │         │              │
│    MAKER     │         │   CHECKER    │         │    ADMIN     │
│              │         │              │         │              │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │                        │                        │
       │ Creates/Submits        │ Reviews/Approves       │ Full Access
       │ Templates              │ Templates              │
       │                        │                        │
       ▼                        ▼                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CheckerController                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  GET  /checker/pending      - List pending templates     │  │
│  │  GET  /checker/verified     - List verified templates    │  │
│  │  GET  /checker/template/:id - Get template details       │  │
│  │  PUT  /checker/approve/:id  - Approve template           │  │
│  │  PUT  /checker/reject/:id   - Reject template            │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Calls
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         CheckerService                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  • getPendingTemplates()                                 │  │
│  │  • getVerifiedTemplates()                                │  │
│  │  • getTemplateById(id)                                   │  │
│  │  • approveTemplate(id, checkerId)  [@Transactional]     │  │
│  │  • rejectTemplate(id, checkerId)   [@Transactional]     │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Uses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Repositories                             │
│  ┌──────────────────────┐  ┌──────────────────────────────┐    │
│  │ RegexTemplateRepo    │  │  AuditLogRepository          │    │
│  │  • findByStatus()    │  │  • save()                    │    │
│  │  • findById()        │  │                              │    │
│  │  • save()            │  │                              │    │
│  └──────────────────────┘  └──────────────────────────────┘    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Accesses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                           Database                               │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐  │
│  │ regex_templates  │  │   audit_logs     │  │    users     │  │
│  │  - template_id   │  │   - audit_id     │  │   - u_id     │  │
│  │  - pattern       │  │   - status       │  │   - name     │  │
│  │  - status        │  │   - verified_by  │  │   - role     │  │
│  │  - created_by    │  │   - verified_at  │  │   - email    │  │
│  │  - bank_id       │  │   - template_id  │  │              │  │
│  └──────────────────┘  └──────────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Template Lifecycle Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Template Lifecycle                            │
└─────────────────────────────────────────────────────────────────┘

1. CREATE DRAFT
   ┌──────────┐
   │  MAKER   │
   └────┬─────┘
        │ POST /regex/save-as-draft
        ▼
   ┌─────────┐
   │  DRAFT  │ ◄─────────────────────┐
   └────┬────┘                       │
        │                            │
        │                            │
2. SUBMIT FOR REVIEW                 │
        │                            │
        │ PUT /regex/push/:id        │
        ▼                            │
   ┌─────────┐                       │
   │ PENDING │                       │
   └────┬────┘                       │
        │                            │
        │                            │
3. CHECKER REVIEWS                   │
   ┌────────────┐                    │
   │  CHECKER   │                    │
   └──────┬─────┘                    │
          │                          │
          │ GET /checker/pending     │
          │ GET /checker/template/:id│
          │                          │
          ├──────────────┬───────────┤
          │              │           │
    APPROVE          REJECT          │
          │              │           │
          │              └───────────┘
          │                          
          │ PUT /checker/approve/:id 
          ▼                          
   ┌──────────┐                      
   │ VERIFIED │                      
   └──────────┘                      
        │                            
        │ Template is now active     
        │ and can be used for        
        │ SMS processing             
        ▼                            
```

## Approval Flow (Detailed)

```
┌─────────────────────────────────────────────────────────────────┐
│              PUT /checker/approve/:templateId                    │
└─────────────────────────────────────────────────────────────────┘

1. REQUEST RECEIVED
   ┌──────────────────────────────────────┐
   │ CheckerController.approveTemplate()  │
   └──────────────┬───────────────────────┘
                  │
2. AUTHORIZATION CHECK
                  ▼
   ┌──────────────────────────────────────┐
   │ Is user CHECKER or ADMIN?            │
   └──────────────┬───────────────────────┘
                  │
                  ├─── NO ──► 401 Unauthorized
                  │
                  └─── YES
                       │
3. SERVICE CALL        ▼
   ┌──────────────────────────────────────┐
   │ CheckerService.approveTemplate()     │
   │ [@Transactional]                     │
   └──────────────┬───────────────────────┘
                  │
4. VALIDATE        ▼
   ┌──────────────────────────────────────┐
   │ Does template exist?                 │
   └──────────────┬───────────────────────┘
                  │
                  ├─── NO ──► 404 Not Found
                  │
                  └─── YES
                       │
                       ▼
   ┌──────────────────────────────────────┐
   │ Is template status PENDING?          │
   └──────────────┬───────────────────────┘
                  │
                  ├─── NO ──► 400 Bad Request
                  │
                  └─── YES
                       │
5. UPDATE STATUS       ▼
   ┌──────────────────────────────────────┐
   │ template.setStatus(VERIFIED)         │
   │ regexTemplateRepository.save()       │
   └──────────────┬───────────────────────┘
                  │
6. CREATE AUDIT LOG    ▼
   ┌──────────────────────────────────────┐
   │ auditLog.setStatus(APPROVED)         │
   │ auditLog.setVerifiedBy(checker)      │
   │ auditLog.setTemplate(template)       │
   │ auditLogRepository.save()            │
   └──────────────┬───────────────────────┘
                  │
7. RETURN RESPONSE     ▼
   ┌──────────────────────────────────────┐
   │ 200 OK + RegexTemplateDto            │
   │ (with status = VERIFIED)             │
   └──────────────────────────────────────┘
```

## Rejection Flow (Detailed)

```
┌─────────────────────────────────────────────────────────────────┐
│              PUT /checker/reject/:templateId                     │
└─────────────────────────────────────────────────────────────────┘

1. REQUEST RECEIVED
   ┌──────────────────────────────────────┐
   │ CheckerController.rejectTemplate()   │
   └──────────────┬───────────────────────┘
                  │
2. AUTHORIZATION CHECK
                  ▼
   ┌──────────────────────────────────────┐
   │ Is user CHECKER or ADMIN?            │
   └──────────────┬───────────────────────┘
                  │
                  ├─── NO ──► 401 Unauthorized
                  │
                  └─── YES
                       │
3. SERVICE CALL        ▼
   ┌──────────────────────────────────────┐
   │ CheckerService.rejectTemplate()      │
   │ [@Transactional]                     │
   └──────────────┬───────────────────────┘
                  │
4. VALIDATE        ▼
   ┌──────────────────────────────────────┐
   │ Does template exist?                 │
   └──────────────┬───────────────────────┘
                  │
                  ├─── NO ──► 404 Not Found
                  │
                  └─── YES
                       │
                       ▼
   ┌──────────────────────────────────────┐
   │ Is template status PENDING?          │
   └──────────────┬───────────────────────┘
                  │
                  ├─── NO ──► 400 Bad Request
                  │
                  └─── YES
                       │
5. UPDATE STATUS       ▼
   ┌──────────────────────────────────────┐
   │ template.setStatus(DRAFT)            │
   │ regexTemplateRepository.save()       │
   └──────────────┬───────────────────────┘
                  │
6. CREATE AUDIT LOG    ▼
   ┌──────────────────────────────────────┐
   │ auditLog.setStatus(REJECTED)         │
   │ auditLog.setVerifiedBy(checker)      │
   │ auditLog.setTemplate(template)       │
   │ auditLogRepository.save()            │
   └──────────────┬───────────────────────┘
                  │
7. RETURN RESPONSE     ▼
   ┌──────────────────────────────────────┐
   │ 200 OK + RegexTemplateDto            │
   │ (with status = DRAFT)                │
   │                                      │
   │ Maker can now revise and resubmit    │
   └──────────────────────────────────────┘
```

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Data Transformation                         │
└─────────────────────────────────────────────────────────────────┘

HTTP Request
    │
    │ JSON
    ▼
┌──────────────┐
│ Controller   │ ◄── Session (userId, userRole)
└──────┬───────┘
       │
       │ Method Call
       ▼
┌──────────────┐
│  Service     │
└──────┬───────┘
       │
       │ Entity Operations
       ▼
┌──────────────┐
│ Repository   │
└──────┬───────┘
       │
       │ SQL Queries
       ▼
┌──────────────┐
│  Database    │
└──────┬───────┘
       │
       │ Entity Objects
       ▼
┌──────────────┐
│  Mapper      │ ──► RegexTemplateMapper.toDto()
└──────┬───────┘
       │
       │ DTO
       ▼
┌──────────────┐
│ Controller   │
└──────┬───────┘
       │
       │ JSON Response
       ▼
HTTP Response
```

## Audit Trail Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                      Audit Trail Creation                        │
└─────────────────────────────────────────────────────────────────┘

Every approve/reject creates an audit log:

BEFORE:
┌──────────────────┐
│ RegexTemplate    │
│  - templateId: 1 │
│  - status: PENDING
│  - auditLog: null│
└──────────────────┘

AFTER APPROVAL:
┌──────────────────┐         ┌──────────────────┐
│ RegexTemplate    │         │   AuditLog       │
│  - templateId: 1 │◄────────│  - auditId: 5    │
│  - status: VERIFIED        │  - status: APPROVED
│  - auditLog: 5   │         │  - verifiedBy: 3 │
└──────────────────┘         │  - verifiedAt: NOW
                             │  - template: 1   │
                             └──────────────────┘

AFTER REJECTION:
┌──────────────────┐         ┌──────────────────┐
│ RegexTemplate    │         │   AuditLog       │
│  - templateId: 2 │◄────────│  - auditId: 6    │
│  - status: DRAFT │         │  - status: REJECTED
│  - auditLog: 6   │         │  - verifiedBy: 3 │
└──────────────────┘         │  - verifiedAt: NOW
                             │  - template: 2   │
                             └──────────────────┘
```

## Security Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    Authorization Check                           │
└─────────────────────────────────────────────────────────────────┘

Request with Session Cookie
         │
         ▼
┌────────────────────┐
│ Session Validation │
└─────────┬──────────┘
          │
          ├─── No session? ──► 401 Unauthorized
          │
          ├─── No token? ──► 401 Unauthorized
          │
          ├─── No userId? ──► 401 Unauthorized
          │
          └─── Has session
                   │
                   ▼
┌────────────────────────────┐
│ Role Check                 │
│ userRole == CHECKER?       │
│ userRole == ADMIN?         │
└─────────┬──────────────────┘
          │
          ├─── NO ──► 401 Unauthorized
          │
          └─── YES
                   │
                   ▼
           Process Request
```

## Complete User Journey

```
┌─────────────────────────────────────────────────────────────────┐
│                    Complete Workflow                             │
└─────────────────────────────────────────────────────────────────┘

DAY 1 - MAKER
─────────────
09:00 │ Maker logs in
      │ POST /auth/login
      │
09:15 │ Creates draft template
      │ POST /regex/save-as-draft
      │ Status: DRAFT
      │
09:30 │ Tests regex pattern
      │ POST /regex/process
      │
10:00 │ Submits for review
      │ PUT /regex/push/:id
      │ Status: DRAFT → PENDING
      │
      │ Maker's work done ✓

DAY 2 - CHECKER
───────────────
10:00 │ Checker logs in
      │ POST /auth/login
      │
10:05 │ Views pending queue
      │ GET /checker/pending
      │ Sees 5 pending templates
      │
10:10 │ Reviews template #1
      │ GET /checker/template/1
      │ Checks pattern, sample msg
      │
10:15 │ Approves template #1
      │ PUT /checker/approve/1
      │ Status: PENDING → VERIFIED
      │ Audit log created ✓
      │
10:20 │ Reviews template #2
      │ GET /checker/template/2
      │ Finds issues in pattern
      │
10:25 │ Rejects template #2
      │ PUT /checker/reject/2
      │ Status: PENDING → DRAFT
      │ Audit log created ✓
      │
      │ Checker's work done ✓

DAY 3 - MAKER (REVISION)
────────────────────────
11:00 │ Maker checks status
      │ GET /regex/:makerId
      │ Sees template #2 is DRAFT
      │
11:05 │ Revises template #2
      │ Fixes the pattern issues
      │
11:10 │ Resubmits template #2
      │ PUT /regex/push/2
      │ Status: DRAFT → PENDING
      │
      │ Back to checker queue

DAY 4 - CHECKER (RE-REVIEW)
───────────────────────────
14:00 │ Checker reviews again
      │ GET /checker/template/2
      │
14:05 │ Approves revised template
      │ PUT /checker/approve/2
      │ Status: PENDING → VERIFIED
      │ Audit log created ✓
      │
      │ Template now active! ✓
```

## Summary

This flow diagram shows:
1. ✅ System architecture and component interactions
2. ✅ Template lifecycle from creation to approval
3. ✅ Detailed approval and rejection flows
4. ✅ Data transformation through layers
5. ✅ Audit trail creation
6. ✅ Security and authorization checks
7. ✅ Complete user journey example

The checker service integrates seamlessly with the existing system and provides a robust approval workflow for regex templates.

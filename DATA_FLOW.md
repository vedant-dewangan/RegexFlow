# RegexFlow - Data Flow Documentation

## Table of Contents
1. [System Architecture Overview](#system-architecture-overview)
2. [Authentication & Session Flow](#authentication--session-flow)
3. [Customer SMS Submission Flow](#customer-sms-submission-flow)
4. [Maker Template Creation Flow](#maker-template-creation-flow)
5. [Checker Review & Approval Flow](#checker-review--approval-flow)
6. [Admin Management Flow](#admin-management-flow)
7. [Database Schema & Relationships](#database-schema--relationships)
8. [API Request/Response Flow](#api-requestresponse-flow)

---

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT BROWSER                           │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              React Frontend (Vite)                      │    │
│  │  - AuthContext (Session Management)                     │    │
│  │  - Axios Config (withCredentials: true)                 │    │
│  │  - Role-based Routing                                   │    │
│  │  - Pages: Dashboard, Maker, Checker, Admin              │    │
│  └──────────────────┬─────────────────────────────────────┘    │
│                     │ HTTP Requests + Session Cookie            │
└─────────────────────┼─────────────────────────────────────────┘
                      │
                      │ CORS Enabled
                      │ (localhost:5173, localhost:3000)
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                           │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐    │
│  │            REST Controllers Layer                       │    │
│  │  - AuthController      - RegexController               │    │
│  │  - SmsController       - CheckerController             │    │
│  │  - BankController      - UserController                │    │
│  └──────────────────┬─────────────────────────────────────┘    │
│                     │                                             │
│  ┌──────────────────▼─────────────────────────────────────┐    │
│  │            Service Layer                                │    │
│  │  - AuthService         - RegexProcessService           │    │
│  │  - SmsService          - CheckerService                │    │
│  │  - BankService         - UserService                   │    │
│  │  - RegexTemplateService                                │    │
│  └──────────────────┬─────────────────────────────────────┘    │
│                     │                                             │
│  ┌──────────────────▼─────────────────────────────────────┐    │
│  │         Repository Layer (JPA/Hibernate)               │    │
│  │  - UserRepository      - RegexTemplateRepository       │    │
│  │  - SmsRepository       - BankRepository                │    │
│  │  - NotificationRepository - AuditLogRepository         │    │
│  └──────────────────┬─────────────────────────────────────┘    │
└─────────────────────┼─────────────────────────────────────────┘
                      │
                      │ JDBC
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                       MySQL DATABASE                             │
│                                                                   │
│  Tables:                                                          │
│  - users                    - regex_template                     │
│  - bank                     - sms                                │
│  - template_request_notification                                 │
│  - audit_log                                                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## Authentication & Session Flow

### 1. User Registration Flow

```
┌─────────┐         ┌──────────┐         ┌─────────────┐        ┌──────────┐
│ Browser │         │ Frontend │         │   Backend   │        │ Database │
└────┬────┘         └────┬─────┘         └──────┬──────┘        └────┬─────┘
     │                   │                       │                     │
     │ 1. Fill Register  │                       │                     │
     │    Form           │                       │                     │
     ├──────────────────►│                       │                     │
     │                   │                       │                     │
     │                   │ 2. POST /auth/register│                     │
     │                   │   {name, email, pwd}  │                     │
     │                   ├──────────────────────►│                     │
     │                   │                       │                     │
     │                   │                       │ 3. Hash Password    │
     │                   │                       │    (BCrypt)         │
     │                   │                       ├─────────┐           │
     │                   │                       │         │           │
     │                   │                       │◄────────┘           │
     │                   │                       │                     │
     │                   │                       │ 4. Save User        │
     │                   │                       │    role=CUSTOMER    │
     │                   │                       ├────────────────────►│
     │                   │                       │                     │
     │                   │                       │◄────────────────────┤
     │                   │                       │ User Entity         │
     │                   │                       │                     │
     │                   │                       │ 5. Generate Token   │
     │                   │                       │    UUID.random()    │
     │                   │                       ├─────────┐           │
     │                   │                       │         │           │
     │                   │                       │◄────────┘           │
     │                   │                       │                     │
     │                   │                       │ 6. Create Session   │
     │                   │                       │    (token, userId,  │
     │                   │                       │     userRole)       │
     │                   │                       ├─────────┐           │
     │                   │                       │         │           │
     │                   │                       │◄────────┘           │
     │                   │                       │                     │
     │                   │ 7. LoginResponse      │                     │
     │                   │    {token, user}      │                     │
     │                   │◄──────────────────────┤                     │
     │                   │                       │                     │
     │                   │ 8. Store in Cookies   │                     │
     │                   │    - token            │                     │
     │                   │    - user (JSON)      │                     │
     │                   ├─────────┐             │                     │
     │                   │         │             │                     │
     │                   │◄────────┘             │                     │
     │                   │                       │                     │
     │                   │ 9. Update AuthContext │                     │
     │                   │    setIsLoggedIn(true)│                     │
     │                   ├─────────┐             │                     │
     │                   │         │             │                     │
     │                   │◄────────┘             │                     │
     │                   │                       │                     │
     │ 10. Navigate to   │                       │                     │
     │     /dashboard    │                       │                     │
     │◄──────────────────┤                       │                     │
     │                   │                       │                     │
```

### 2. User Login Flow

```
┌─────────┐         ┌──────────┐         ┌─────────────┐        ┌──────────┐
│ Browser │         │ Frontend │         │   Backend   │        │ Database │
└────┬────┘         └────┬─────┘         └──────┬──────┘        └────┬─────┘
     │                   │                       │                     │
     │ 1. Fill Login     │                       │                     │
     │    Form           │                       │                     │
     ├──────────────────►│                       │                     │
     │                   │                       │                     │
     │                   │ 2. POST /auth/login   │                     │
     │                   │    {email, password}  │                     │
     │                   ├──────────────────────►│                     │
     │                   │                       │                     │
     │                   │                       │ 3. Find User        │
     │                   │                       │    by Email         │
     │                   │                       ├────────────────────►│
     │                   │                       │                     │
     │                   │                       │◄────────────────────┤
     │                   │                       │ User Entity         │
     │                   │                       │                     │
     │                   │                       │ 4. Verify Password  │
     │                   │                       │    BCrypt.matches() │
     │                   │                       ├─────────┐           │
     │                   │                       │         │           │
     │                   │                       │◄────────┘           │
     │                   │                       │                     │
     │                   │                       │ 5. Generate Token   │
     │                   │                       │    & Create Session │
     │                   │                       ├─────────┐           │
     │                   │                       │         │           │
     │                   │                       │◄────────┘           │
     │                   │                       │                     │
     │                   │ 6. LoginResponse      │                     │
     │                   │    {token, sessionId, │                     │
     │                   │     user{id,name,     │                     │
     │                   │     email,role}}      │                     │
     │                   │◄──────────────────────┤                     │
     │                   │                       │                     │
     │                   │ 7. Store Credentials  │                     │
     │                   │    in Cookies         │                     │
     │                   ├─────────┐             │                     │
     │                   │         │             │                     │
     │                   │◄────────┘             │                     │
     │                   │                       │                     │
     │ 8. Redirect to    │                       │                     │
     │    Role Dashboard │                       │                     │
     │◄──────────────────┤                       │                     │
     │                   │                       │                     │
```

### 3. Session-Based Authentication

**Every API request includes:**
- **Session Cookie** (JSESSIONID) - Automatically sent by browser with `withCredentials: true`
- Backend verifies session attributes: `token`, `userId`, `userRole`
- If invalid or expired → 401/403 → Frontend redirects to login

---

## Customer SMS Submission Flow

### Complete Data Flow: SMS Submit → Parse → Extract → Store

```
┌──────────┐       ┌──────────┐       ┌─────────────┐       ┌──────────────┐
│ Customer │       │ Frontend │       │   Backend   │       │   Database   │
│Dashboard │       │ (React)  │       │   (Spring)  │       │   (MySQL)    │
└────┬─────┘       └────┬─────┘       └──────┬──────┘       └──────┬───────┘
     │                  │                     │                      │
     │ 1. User enters   │                     │                      │
     │    SMS text      │                     │                      │
     ├─────────────────►│                     │                      │
     │                  │                     │                      │
     │                  │ 2. POST /sms/submit │                      │
     │                  │    {smsText}        │                      │
     │                  ├────────────────────►│                      │
     │                  │                     │                      │
     │                  │              ┌──────▼───────┐              │
     │                  │              │ SmsService   │              │
     │                  │              │.processSms() │              │
     │                  │              └──────┬───────┘              │
     │                  │                     │                      │
     │                  │                     │ 3. Extract Sender    │
     │                  │                     │    Header            │
     │                  │                     │    (before first :)  │
     │                  │                     ├─────────┐            │
     │                  │                     │         │            │
     │                  │                     │◄────────┘            │
     │                  │                     │                      │
     │                  │                     │ 4. Find VERIFIED     │
     │                  │                     │    templates for     │
     │                  │                     │    senderHeader      │
     │                  │                     ├─────────────────────►│
     │                  │                     │                      │
     │                  │                     │◄─────────────────────┤
     │                  │                     │ List<RegexTemplate>  │
     │                  │                     │                      │
     │         ┌────────┴─────────────────────┴──────┐               │
     │         │   NO TEMPLATES FOUND                │               │
     │         │                                      │               │
     │         │  5a. Save SMS (no match)            │               │
     │         │  5b. Create Template Request        │               │
     │         │      Notification (PENDING)         │               │
     │         │  5c. Return {hasMatch: false}       │               │
     │         └────────┬─────────────────────┬──────┘               │
     │                  │                     │                      │
     │         ┌────────┴─────────────────────┴──────┐               │
     │         │   TEMPLATES FOUND                   │               │
     │         │                                      │               │
     │         │  For each template:                 │               │
     │         │                                      │               │
     │         │  ┌────────────────────────────────┐ │               │
     │         │  │ 6. RegexProcessService         │ │               │
     │         │  │    .processRegex()             │ │               │
     │         │  │                                │ │               │
     │         │  │ a. Compile pattern             │ │               │
     │         │  │    Pattern.compile(regex)      │ │               │
     │         │  │                                │ │               │
     │         │  │ b. matcher.find() on SMS       │ │               │
     │         │  │                                │ │               │
     │         │  │ c. Extract named groups:       │ │               │
     │         │  │    - amount                    │ │               │
     │         │  │    - date                      │ │               │
     │         │  │    - merchant                  │ │               │
     │         │  │    - balance                   │ │               │
     │         │  │    - bankAcId                  │ │               │
     │         │  │    ... (35 known fields)       │ │               │
     │         │  │                                │ │               │
     │         │  │ d. Return RegexProcessResponse │ │               │
     │         │  │    {field: {value, index}}     │ │               │
     │         │  └────────────────────────────────┘ │               │
     │         │                                      │               │
     │         │  7. Count non-null extracted fields │               │
     │         │     for each template              │               │
     │         │                                      │               │
     │         │  8. Select template with MAX fields │               │
     │         │     (best match)                    │               │
     │         │                                      │               │
     │         │  ┌─────────────────────────────────┐│               │
     │         │  │ NO MATCH (all 0 fields)         ││               │
     │         │  │                                 ││               │
     │         │  │ 9a. Save SMS (no match)         ││               │
     │         │  │ 9b. Create Notification         ││               │
     │         │  │ 9c. Return {hasMatch: false}    ││               │
     │         │  └─────────────────────────────────┘│               │
     │         │                                      │               │
     │         │  ┌─────────────────────────────────┐│               │
     │         │  │ MATCH FOUND                     ││               │
     │         │  │                                 ││               │
     │         │  │ 10. Convert to                  ││               │
     │         │  │     ExtractedFieldsDto          ││               │
     │         │  │                                 ││               │
     │         │  │ 11. Save SMS with:              ││               │
     │         │  │     - matchedTemplate           ││               │
     │         │  │     - extractedFields (JSON)    ││               │
     │         │  │     - user reference            ││               │
     │         │  └─────────────────────────────────┘│               │
     │         └────────┬─────────────────────┬──────┘               │
     │                  │                     │                      │
     │                  │                     │ 12. Save to DB       │
     │                  │                     ├─────────────────────►│
     │                  │                     │                      │
     │                  │                     │◄─────────────────────┤
     │                  │                     │ SMS Entity           │
     │                  │                     │                      │
     │                  │ 13. SmsSubmissionResponse                  │
     │                  │     {smsId, hasMatch,                      │
     │                  │      extractedFields:{                     │
     │                  │        amount,                             │
     │                  │        date,                               │
     │                  │        merchant,                           │
     │                  │        balance,                            │
     │                  │        ...                                 │
     │                  │      }}                                    │
     │                  │◄────────────────────┤                      │
     │                  │                     │                      │
     │ 14. Display      │                     │                      │
     │     Extracted    │                     │                      │
     │     Fields Card  │                     │                      │
     │◄─────────────────┤                     │                      │
     │                  │                     │                      │
     │ 15. Refresh      │                     │                      │
     │     History      │                     │                      │
     │     (GET         │                     │                      │
     │     /sms/history)│                     │                      │
     │◄─────────────────┤                     │                      │
     │                  │                     │                      │
```

### SMS Data Structure in Database

```
sms Table:
┌──────────────┬────────────────────────────────────────────┐
│ Field        │ Description                                │
├──────────────┼────────────────────────────────────────────┤
│ sms_id       │ Primary Key (Auto-generated)               │
│ sms_text     │ Original SMS text                          │
│ sender_header│ Extracted sender (e.g., "ABCBANK")         │
│ user_id      │ Foreign Key → users table                  │
│ template_id  │ Foreign Key → regex_template (if matched)  │
│ has_match    │ Boolean (true if template matched)         │
│ extracted_   │ JSON blob of parsed fields                 │
│ fields_json  │ {amount: "500", merchant: "ABC", ...}      │
│ created_at   │ Timestamp                                  │
└──────────────┴────────────────────────────────────────────┘
```

---

## Maker Template Creation Flow

### Complete Workflow: Create → Draft → Test → Push → Pending

```
┌──────────┐     ┌──────────┐     ┌─────────────┐     ┌──────────┐
│  Maker   │     │ Frontend │     │   Backend   │     │ Database │
│Dashboard │     │          │     │             │     │          │
└────┬─────┘     └────┬─────┘     └──────┬──────┘     └────┬─────┘
     │                │                   │                  │
     │ 1. View        │                   │                  │
     │    Template    │                   │                  │
     │    Notifications│                  │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 2. GET /sms/      │                  │
     │                │    notifications/ │                  │
     │                │    pending        │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 3. Find PENDING  │
     │                │                   │    notifications │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │ List<Notification>
     │                │                   │                  │
     │                │ 4. List of        │                  │
     │                │    notifications  │                  │
     │                │    {senderHeader, │                  │
     │                │     smsText,      │                  │
     │                │     requestedBy}  │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 5. Click       │                   │                  │
     │    "Create     │                   │                  │
     │    Template"   │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 6. Navigate to    │                  │
     │                │    Template Editor│                  │
     │                │    with prefill   │                  │
     │                ├─────────┐         │                  │
     │                │         │         │                  │
     │                │◄────────┘         │                  │
     │                │                   │                  │
     │ 7. Fill Form:  │                   │                  │
     │    - Bank      │                   │                  │
     │    - Sender    │                   │                  │
     │    - Pattern   │                   │                  │
     │    - Sample    │                   │                  │
     │    - SMS Type  │                   │                  │
     │    - Payment   │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │ 8. Click       │                   │                  │
     │    "Test Regex"│                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 9. POST /regex/   │                  │
     │                │    process        │                  │
     │                │    {regexPattern, │                  │
     │                │     rawMsg,       │                  │
     │                │     smsType,      │                  │
     │                │     paymentType}  │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │            ┌──────▼───────┐          │
     │                │            │RegexProcess  │          │
     │                │            │Service       │          │
     │                │            │.processRegex()│         │
     │                │            └──────┬───────┘          │
     │                │                   │                  │
     │                │                   │ 10. Compile &    │
     │                │                   │     Match        │
     │                │                   ├─────────┐        │
     │                │                   │         │        │
     │                │                   │◄────────┘        │
     │                │                   │                  │
     │                │ 11. Extracted     │                  │
     │                │     Fields        │                  │
     │                │     {amount:{},   │                  │
     │                │      date:{},     │                  │
     │                │      merchant:{}} │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 12. Preview    │                   │                  │
     │     Extracted  │                   │                  │
     │     Fields     │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
     │ 13. Click      │                   │                  │
     │     "Save as   │                   │                  │
     │     Draft"     │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 14. POST /regex/  │                  │
     │                │     save-as-draft │                  │
     │                │     {RegexTemplate│                  │
     │                │      Dto}         │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 15. Create       │
     │                │                   │     RegexTemplate│
     │                │                   │     status=DRAFT │
     │                │                   │     maker_id     │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │ Template Entity  │
     │                │                   │                  │
     │                │ 16. Created       │                  │
     │                │     Template DTO  │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 17. Success    │                   │                  │
     │     Message    │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
     │                │                   │                  │
     │ 18. (Later)    │                   │                  │
     │     Click "Push│                   │                  │
     │     for Review"│                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 19. PUT /regex/   │                  │
     │                │     push/{id}     │                  │
     │                │     {RegexTemplate│                  │
     │                │      Dto with     │                  │
     │                │      sampleRawMsg}│                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 20. Update       │
     │                │                   │     status=      │
     │                │                   │     PENDING      │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │                  │
     │                │ 21. Updated       │                  │
     │                │     Template      │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 22. Now in     │                   │                  │
     │     PENDING    │                   │                  │
     │     state      │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
```

### Template Lifecycle States

```
┌─────────────────────────────────────────────────────────────┐
│                  TEMPLATE STATUS FLOW                        │
└─────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  DRAFT   │  ◄── Initial state when maker saves
    └─────┬────┘
          │
          │ Maker: "Push for Review"
          │ (PUT /regex/push/{id})
          │
          ▼
    ┌──────────┐
    │ PENDING  │  ◄── Waiting for Checker review
    └─────┬────┘
          │
          ├─────────────────┬─────────────────┐
          │                 │                 │
          │ Checker:        │ Checker:        │
          │ Approve         │ Reject          │
          │                 │                 │
          ▼                 ▼                 ▼
    ┌──────────┐      ┌──────────┐     ┌──────────┐
    │ VERIFIED │      │  DRAFT   │     │DEPRECATED│
    └──────────┘      └──────────┘     └──────────┘
         │                  │
         │                  │
         │                  └──► Can be edited
         │                      and pushed again
         │
         └──► Active template
              Used for SMS matching
```

---

## Checker Review & Approval Flow

### Complete Workflow: View Pending → Test → Approve/Reject → Audit

```
┌──────────┐     ┌──────────┐     ┌─────────────┐     ┌──────────┐
│ Checker  │     │ Frontend │     │   Backend   │     │ Database │
│Dashboard │     │          │     │             │     │          │
└────┬─────┘     └────┬─────┘     └──────┬──────┘     └────┬─────┘
     │                │                   │                  │
     │ 1. View        │                   │                  │
     │    Pending     │                   │                  │
     │    Templates   │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 2. GET /checker/  │                  │
     │                │    pending        │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 3. Find templates│
     │                │                   │    status=PENDING│
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │ List<Template>   │
     │                │                   │                  │
     │                │ 4. Pending        │                  │
     │                │    Templates      │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 5. Click       │                   │                  │
     │    "Test Regex"│                   │                  │
     │    on a template│                  │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 6. Open Test Modal│                  │
     │                │    with template  │                  │
     │                │    pattern &      │                  │
     │                │    sample message │                  │
     │                ├─────────┐         │                  │
     │                │         │         │                  │
     │                │◄────────┘         │                  │
     │                │                   │                  │
     │ 7. (Optional)  │                   │                  │
     │    Edit message│                   │                  │
     │    Click "Test"│                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 8. POST /regex/   │                  │
     │                │    process        │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 9. Process regex │
     │                │                   ├─────────┐        │
     │                │                   │         │        │
     │                │                   │◄────────┘        │
     │                │                   │                  │
     │                │ 10. Extracted     │                  │
     │                │     Fields        │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 11. View       │                   │                  │
     │     Results    │                   │                  │
     │     Table      │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
     │                │                   │                  │
     │ ═══════════════ APPROVE FLOW ═══════════════         │
     │                │                   │                  │
     │ 12. Click      │                   │                  │
     │     "Approve"  │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 13. PUT /checker/ │                  │
     │                │     approve/{id}  │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │            ┌──────▼───────┐          │
     │                │            │CheckerService│          │
     │                │            │.approveTemplate         │
     │                │            └──────┬───────┘          │
     │                │                   │                  │
     │                │                   │ 14. Update       │
     │                │                   │     template     │
     │                │                   │     status=      │
     │                │                   │     VERIFIED     │
     │                │                   │     checked_by   │
     │                │                   │     checked_at   │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │ 15. Create       │
     │                │                   │     AuditLog     │
     │                │                   │     action=      │
     │                │                   │     APPROVED     │
     │                │                   │     checker_id   │
     │                │                   │     template_id  │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │                  │
     │                │ 16. Updated       │                  │
     │                │     Template DTO  │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 17. Success    │                   │                  │
     │     "Template  │                   │                  │
     │     Approved"  │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
     │                │                   │                  │
     │ ═══════════════ REJECT FLOW ════════════════         │
     │                │                   │                  │
     │ 18. Click      │                   │                  │
     │     "Reject"   │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 19. PUT /checker/ │                  │
     │                │     reject/{id}   │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 20. Update       │
     │                │                   │     template     │
     │                │                   │     status=DRAFT │
     │                │                   │     checked_by   │
     │                │                   │     checked_at   │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │ 21. Create       │
     │                │                   │     AuditLog     │
     │                │                   │     action=      │
     │                │                   │     REJECTED     │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │                  │
     │                │ 22. Updated       │                  │
     │                │     Template DTO  │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 23. Success    │                   │                  │
     │     "Template  │                   │                  │
     │     Rejected"  │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
```

---

## Admin Management Flow

### 1. User Management Flow

```
┌──────────┐     ┌──────────┐     ┌─────────────┐     ┌──────────┐
│  Admin   │     │ Frontend │     │   Backend   │     │ Database │
│Dashboard │     │          │     │             │     │          │
└────┬─────┘     └────┬─────┘     └──────┬──────┘     └────┬─────┘
     │                │                   │                  │
     │ 1. View All    │                   │                  │
     │    Users       │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 2. GET /user      │                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 3. Check role    │
     │                │                   │    session.role  │
     │                │                   │    == ADMIN      │
     │                │                   ├─────────┐        │
     │                │                   │         │        │
     │                │                   │◄────────┘        │
     │                │                   │                  │
     │                │                   │ 4. Get all users │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │ List<Users>      │
     │                │                   │                  │
     │                │ 5. List of Users  │                  │
     │                │    (UserResponse) │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 6. Display     │                   │                  │
     │    Users Table │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
     │ 7. Click       │                   │                  │
     │    "Update     │                   │                  │
     │    Role" for   │                   │                  │
     │    a user      │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │ 8. Select new  │                   │                  │
     │    role (MAKER,│                   │                  │
     │    CHECKER)    │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 9. PUT /user/{id} │                  │
     │                │    {role: "MAKER"}│                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 10. Validate:    │
     │                │                   │     - Not ADMIN  │
     │                │                   │     - User exists│
     │                │                   ├─────────┐        │
     │                │                   │         │        │
     │                │                   │◄────────┘        │
     │                │                   │                  │
     │                │                   │ 11. Update user  │
     │                │                   │     role         │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │                  │
     │                │ 12. Updated User  │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 13. Success    │                   │                  │
     │     Message &  │                   │                  │
     │     Refresh    │                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
```

### 2. Bank Management Flow

```
┌──────────┐     ┌──────────┐     ┌─────────────┐     ┌──────────┐
│  Admin   │     │ Frontend │     │   Backend   │     │ Database │
└────┬─────┘     └────┬─────┘     └──────┬──────┘     └────┬─────┘
     │                │                   │                  │
     │ 1. Click       │                   │                  │
     │    "Add Bank"  │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │ 2. Fill Form:  │                   │                  │
     │    - Bank Name │                   │                  │
     │    - Address   │                   │                  │
     ├───────────────►│                   │                  │
     │                │                   │                  │
     │                │ 3. POST /bank/    │                  │
     │                │    create         │                  │
     │                │    {name, address}│                  │
     │                ├──────────────────►│                  │
     │                │                   │                  │
     │                │                   │ 4. Validate &    │
     │                │                   │    Create Bank   │
     │                │                   ├─────────────────►│
     │                │                   │                  │
     │                │                   │◄─────────────────┤
     │                │                   │ Bank Entity      │
     │                │                   │                  │
     │                │ 5. Created Bank   │                  │
     │                │◄──────────────────┤                  │
     │                │                   │                  │
     │ 6. Success &   │                   │                  │
     │    Refresh List│                   │                  │
     │◄───────────────┤                   │                  │
     │                │                   │                  │
```

---

## Database Schema & Relationships

### Entity-Relationship Diagram

```
┌─────────────────────┐
│       users         │
│─────────────────────│
│ user_id (PK)        │◄──────────┐
│ name                │           │
│ email (UNIQUE)      │           │
│ password_hash       │           │
│ role (ENUM)         │           │
│ created_at          │           │
└─────────────────────┘           │
         △                        │
         │                        │
         │                        │
         │ made_by                │ user_id
         │                        │
┌────────┴────────────┐     ┌─────┴───────────────┐
│  regex_template     │     │        sms          │
│─────────────────────│     │─────────────────────│
│ template_id (PK)    │     │ sms_id (PK)         │
│ sender_header       │     │ sms_text            │
│ pattern             │◄───┐│ sender_header       │
│ sample_raw_msg      │    ││ user_id (FK)        │
│ bank_id (FK)        │    ││ template_id (FK)    │
│ sms_type (ENUM)     │    ││ has_match           │
│ transaction_type    │    ││ extracted_fields_   │
│ payment_type (ENUM) │    ││ json                │
│ status (ENUM)       │    ││ created_at          │
│ maker_id (FK)       │────┘└─────────────────────┘
│ checker_id (FK)     │
│ checked_at          │
│ created_at          │
│ updated_at          │
└─────────────────────┘
         △
         │
         │ bank_id
         │
┌────────┴────────────┐
│        bank         │
│─────────────────────│
│ bank_id (PK)        │
│ name                │
│ address             │
│ is_active           │
│ created_at          │
└─────────────────────┘


┌─────────────────────────────────┐
│ template_request_notification   │
│─────────────────────────────────│
│ notification_id (PK)             │
│ sender_header                    │
│ sms_text                         │
│ requested_by (FK → users)        │
│ status (PENDING/RESOLVED)        │
│ created_at                       │
│ resolved_at                      │
└─────────────────────────────────┘


┌─────────────────────────────────┐
│         audit_log               │
│─────────────────────────────────│
│ audit_id (PK)                   │
│ template_id (FK)                │
│ action (APPROVED/REJECTED)      │
│ performed_by (FK → users)       │
│ performed_at                    │
│ comments                        │
└─────────────────────────────────┘
```

### Key Relationships

| Relationship | Type | Description |
|--------------|------|-------------|
| **users ↔ regex_template** | One-to-Many | A maker creates multiple templates |
| **users ↔ sms** | One-to-Many | A customer submits multiple SMS |
| **regex_template ↔ sms** | One-to-Many | A template matches multiple SMS |
| **bank ↔ regex_template** | One-to-Many | A bank has multiple templates |
| **users ↔ audit_log** | One-to-Many | A checker performs multiple audits |
| **regex_template ↔ audit_log** | One-to-Many | A template has multiple audit entries |

---

## API Request/Response Flow

### Typical Request Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT SIDE (Browser)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 1. User Action (Click, Submit)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   React Component                                │
│  - useState hooks for form data                                  │
│  - Event handlers (onClick, onSubmit)                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 2. Call axios function
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Axios Instance                                 │
│  - Base URL: http://localhost:8080                               │
│  - withCredentials: true (sends session cookie)                  │
│  - Headers: Content-Type: application/json                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 3. HTTP Request
                              │    (GET/POST/PUT/DELETE)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND SIDE (Spring Boot)                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 4. CorsFilter (allow origin)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SecurityConfig                                 │
│  - permitAll: /, /auth/**, /error                                │
│  - authenticated: all other requests                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 5. Check HttpSession
                              │    (token, userId, userRole)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   REST Controller                                │
│  - @RestController, @RequestMapping                              │
│  - Method with @GetMapping/@PostMapping/etc.                     │
│  - Extract: @RequestBody, @PathVariable, HttpSession             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 6. Validate role
                              │    (isAdmin(), isMaker(), etc.)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Service Layer                                  │
│  - Business logic                                                │
│  - Data transformation                                           │
│  - Call repositories                                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 7. Database operations
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   JPA Repository                                 │
│  - findBy...(), save(), delete()                                 │
│  - Query methods, @Query                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 8. SQL queries
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   MySQL Database                                 │
│  - Execute query                                                 │
│  - Return entity/entities                                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 9. Return data
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Service Layer                                  │
│  - Map Entity → DTO                                              │
│  - Apply business rules                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 10. Return DTO
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   REST Controller                                │
│  - Wrap in ResponseEntity                                        │
│  - Set HTTP status (200, 201, 400, etc.)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 11. HTTP Response (JSON)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Axios Interceptor                              │
│  - Check status code                                             │
│  - 401/403 → logout & redirect                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 12. Response data
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   React Component                                │
│  - setState with response data                                   │
│  - Update UI                                                     │
│  - Show toast notification                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ 13. Re-render
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Browser Display                                │
│  - Updated component with new data                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Summary of Data Flows by Role

### Customer Flow
```
Login → Dashboard → Submit SMS → Extract Fields → View History → Monthly Expense
```

**Key Data Entities:**
- User (CUSTOMER role)
- SMS (submitted text + extracted fields)
- Template Request Notification (if no match)

### Maker Flow
```
Login → Maker Dashboard → View Notifications → Create Template → 
Test Regex → Save Draft → Push for Review
```

**Key Data Entities:**
- User (MAKER role)
- Regex Template (DRAFT → PENDING)
- Template Request Notification (RESOLVED)
- Bank (for template association)

### Checker Flow
```
Login → Checker Dashboard → View Pending → Test Regex → 
Approve/Reject → Audit Log
```

**Key Data Entities:**
- User (CHECKER role)
- Regex Template (PENDING → VERIFIED/DRAFT)
- Audit Log (APPROVED/REJECTED action)

### Admin Flow
```
Login → Admin Dashboard → Manage Users → Create Banks → 
View All Templates → Update User Roles
```

**Key Data Entities:**
- User (ADMIN role)
- All Users (role management)
- All Banks
- All Templates

---

## End-to-End Data Flow Example

### Scenario: Customer SMS Processing with Regex Matching

```
1. Customer submits SMS: "ABCBANK: Your account X1234 debited Rs.500 
   on 01-Jan-2024 at ABC Store. Balance: Rs.10000"

2. Backend extracts sender: "ABCBANK"

3. Backend loads VERIFIED templates for "ABCBANK":
   - Template #1: Pattern for debit
   - Template #2: Pattern for credit
   - Template #3: Pattern for balance inquiry

4. For each template, run regex:
   - Template #1 extracts: {amount: "500", date: "01-Jan-2024", 
     merchant: "ABC Store", balance: "10000", bankAcId: "X1234"}
     → 5 fields extracted
   - Template #2 extracts: {} → 0 fields (credit pattern doesn't match)
   - Template #3 extracts: {balance: "10000"} → 1 field

5. Best match: Template #1 (5 fields)

6. Save SMS with:
   - matched_template_id = Template #1
   - extracted_fields_json = {amount: "500", ...}
   - has_match = true

7. Return to frontend:
   {
     "smsId": 123,
     "hasMatch": true,
     "extractedFields": {
       "amount": "500",
       "date": "01-Jan-2024",
       "merchant": "ABC Store",
       "balance": "10000",
       "transactionType": "DEBIT"
     }
   }

8. Frontend displays ExtractedFieldsCard with all fields
```

---

## Performance Considerations

### Time Complexity Analysis

| Operation | Complexity | Variables |
|-----------|------------|-----------|
| **SMS Processing** | O(T × (P + M × P)) | T = templates, P = pattern length, M = message length |
| **Regex Compilation** | O(P) | Per template |
| **Regex Matching** | O(M × P) typical | Can be exponential in worst case |
| **Field Extraction** | O(G) | G = 35 known fields |
| **Database Queries** | O(log N) | With proper indexes |
| **Template Listing** | O(N) | N = total templates |

### Optimization Strategies

1. **Database Indexes:**
   - `sender_header` on `regex_template` table
   - `status` on `regex_template` table
   - `user_id` on `sms` table
   - Composite index: `(sender_header, status)` for fast template lookup

2. **Caching (Future Enhancement):**
   - Cache compiled regex patterns (Pattern objects)
   - Cache VERIFIED templates by sender header
   - Redis for session management

3. **Regex Best Practices:**
   - Avoid catastrophic backtracking patterns
   - Use non-capturing groups `(?:...)` when group value not needed
   - Use atomic groups for performance
   - Limit quantifier ranges

---

## Security & Session Management

### Session Data Structure

```javascript
HttpSession attributes:
{
  "token": "f47ac10b-58cc-4372-a567-0e02b2c3d479",  // UUID
  "userId": 5,                                       // Long
  "userRole": "MAKER"                                // String (enum name)
}
```

### Frontend Cookie Storage

```javascript
Cookies:
- "token": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
- "user": '{"userId":5,"name":"John","email":"john@mail.com","role":"MAKER"}'

Used by AuthContext to:
1. Check if user is authenticated (isAuthenticated())
2. Get user details (getUser())
3. Send with API requests (withCredentials: true)
```

### Role-Based Access Control

| Role | Access Level | Protected Routes |
|------|--------------|------------------|
| **CUSTOMER** | Basic | `/dashboard/*` |
| **MAKER** | Template Creation | `/maker/*`, `/dashboard/*` |
| **CHECKER** | Template Review | `/checker/*`, `/maker/*` (read-only), `/dashboard/*` |
| **ADMIN** | Full Access | `/admin/*`, all other routes |

---

*This comprehensive data flow documentation covers all major workflows in the RegexFlow application. Each flow shows the complete path from user action through frontend, backend, and database layers.*

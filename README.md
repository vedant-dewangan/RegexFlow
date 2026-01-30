# RegexFlow

**RegexFlow** is a full-stack application for parsing bank and SMS transaction messages using regex templates. It implements a **Maker–Checker** workflow: makers create regex templates, checkers approve or reject them, and customers submit SMS text that is matched against verified templates to extract transaction fields (amount, date, merchant, balance, etc.).

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [User Roles and Access](#2-user-roles-and-access)
3. [End-to-End Workflow](#3-end-to-end-workflow)
4. [Time Complexity Analysis](#4-time-complexity-analysis)
5. [API Reference](#5-api-reference)
6. [How the Parser Works](#6-how-the-parser-works)
7. [SMS Matching Flow](#7-sms-matching-flow)
8. [Frontend Flow](#8-frontend-flow)
9. [Setup and Run](#9-setup-and-run)
10. [AI Tools Used](#10-ai-tools-used)

---

## 1. Architecture Overview

| Layer        | Technology                    | Purpose                                                                 |
|-------------|-------------------------------|-------------------------------------------------------------------------|
| **Frontend** | React (Vite), React Router, Axios | UI, auth, role-based dashboards, API calls with session cookies         |
| **Backend**  | Spring Boot 3, Java 17         | REST API, session-based auth, business logic, regex processing          |
| **Database** | MySQL (JPA/Hibernate)        | Users, banks, regex templates, SMS records, notifications, audit logs  |

- **Authentication**: Session-based. Backend stores `token`, `userId`, and `userRole` in **HttpSession** after login. Frontend stores token and user in **cookies** and sends **`withCredentials: true`** on every request so the session cookie is sent.
- **CORS**: Backend allows `http://localhost:5173` and `http://localhost:3000` with credentials (`CorsConfig`).

---

## 2. User Roles and Access

| Role       | Description | Access |
|-----------|-------------|--------|
| **CUSTOMER** | End users who submit SMS | `/dashboard`, `/dashboard/transactions`, `/dashboard/monthly-expense` — submit SMS, view history and monthly expense |
| **MAKER**    | Creates regex templates   | `/maker/dashboard`, `/maker/template/new`, `/maker/template/:id` — create/edit drafts, push for review, see template-request notifications |
| **CHECKER**  | Reviews templates         | `/checker/dashboard` — view pending/verified templates, approve/reject, test regex |
| **ADMIN**    | Full management           | `/admin`, `/admin/user`, `/admin/bank`, `/admin/regex` — manage users, banks, view all templates |

Route protection is done via **RoleBasedRoute**: if the user is not logged in they are redirected to `/login`; if logged in with a different role they are redirected to their role’s dashboard.

---

## 3. End-to-End Workflow

### 3.1 Customer Submits SMS

1. Customer opens **Dashboard**, enters SMS text (e.g. a bank debit/credit message), clicks **Submit**.
2. Frontend sends **`POST /sms/submit`** with `{ "smsText": "..." }`.
3. Backend (**SmsService**):
   - Extracts **sender header** (text before first `:`).
   - Loads all **VERIFIED** templates with that sender header.
   - If none: saves SMS, creates a **Template Request Notification** (PENDING), returns “No template, maker notified”.
   - If some: runs each template’s regex on the SMS via **RegexProcessService**, picks the template that extracts the **most** non-null fields (“best match”).
   - If no template matches (all regexes fail or extract 0 fields): same as “none” — save SMS, create notification.
   - If a template matches: saves SMS with `matchedTemplate` and extracted fields as JSON; returns `hasMatch: true` and `extractedFields`.
4. Frontend shows either the extracted fields (amount, date, merchant, balance, etc.) or the “no template” message and refreshes transaction history.

### 3.2 Maker Creates and Pushes a Template

1. Maker opens **Maker Dashboard**. They can open **Create Template** from a **Template Request Notification** (prefilled with sender and sample SMS).
2. In **Template Editor**:
   - Selects **Bank** (from `GET /bank`).
   - Enters **Sender Header** (e.g. `ABCBANK`).
   - Enters **Regex Pattern** with **named capturing groups**, e.g. `(?<amount>[\d.]+)`, or uses **AI Generate** (Gemini) to create a pattern from the Sample Raw Message.
   - Enters **Sample Raw Message** (required when pushing).
   - Chooses **SMS Type**, **Transaction Type**, **Payment Type**.
3. **Save as Draft**: **`POST /regex/save-as-draft`** — template is stored with status **DRAFT**.
4. **Push for Review**: **`PUT /regex/push/{templateId}`** — status changes to **PENDING** (only DRAFT templates can be pushed).
5. Maker can **Test Regex** in the editor: **`POST /regex/process`** with pattern and sample message; response shows extracted fields.

### 3.3 Checker Approves or Rejects

1. Checker opens **Checker Dashboard**. **`GET /checker/pending`** returns all templates with status **PENDING**.
2. For each pending template, checker can:
   - **Test Regex**: opens modal, calls **`POST /regex/process`** with template’s pattern and (editable) message; sees extracted fields.
   - **Approve**: **`PUT /checker/approve/{templateId}`** — status → **VERIFIED**, **AuditLog** created (APPROVED, checker, timestamp).
   - **Reject**: **`PUT /checker/reject/{templateId}`** — status → **DRAFT** (maker can edit again), AuditLog (REJECTED).
3. **`GET /checker/verified`** lists all VERIFIED templates.

### 3.4 Template Lifecycle (Status Flow)

```
DRAFT  →  (Maker: Push for review)  →  PENDING
PENDING  →  (Checker: Approve)       →  VERIFIED
PENDING  →  (Checker: Reject)        →  DRAFT
```

Only **VERIFIED** templates are used when a customer submits an SMS. Only **DRAFT** templates can be edited and pushed to PENDING.

### 3.5 No Template for Customer SMS

1. Customer submits SMS; no VERIFIED template exists for that sender, or no regex matches.
2. Backend saves the SMS and creates **TemplateRequestNotification** (PENDING).
3. Maker sees it under **Template Request Notifications**; can **Create Template** (prefill sender + sample SMS) or **Mark Resolved** (**`PUT /sms/notifications/{id}/resolve`**).

---

## 4. Time Complexity Analysis

### 4.1 Regex Parser (RegexProcessService)

| Step | Operation | Time Complexity | Notes |
|------|-----------|------------------|--------|
| 1 | `Pattern.compile(regex)` | **O(P)** | P = pattern length. Builds NFA/DFA. |
| 2 | `findGroupNumbers(regex)` | **O(P)** | Single pass over pattern; counts named groups `(?<name>...)`. |
| 3 | `matcher.find()` | **O(M × P)** typical; **exponential** in worst case | M = message length. Java regex can backtrack badly on pathological patterns. |
| 4 | Field extraction loop | **O(G)** | G = number of known field names (~35). Each `matcher.group(name)` is O(1) after match. |

**Overall for one regex run**: **O(P + M × P + G)** ≈ **O(P + M × P)** in practice, with a caveat for worst-case regex.

### 4.2 SMS Submission (SmsService.processSms)

| Step | Operation | Time Complexity | Notes |
|------|-----------|------------------|--------|
| 1 | Extract sender header | **O(M)** | M = SMS length; scan until first `:`. |
| 2 | DB: find VERIFIED templates by sender | **O(T)** | T = number of such templates (depends on DB index). |
| 3 | For each template: compile + match + count fields | **O(T × (P + M × P + F))** | P = pattern length, F = field count. Compilation per template. |
| 4 | Pick best match, convert to DTO, save SMS | **O(T + M + F)** | Dominated by step 3. |

**Overall**: **O(T × (P + M × P))** for the matching phase; DB and serialization add linear terms.

### 4.3 Other API Operations

| Operation | Complexity | Notes |
|-----------|------------|--------|
| GET all templates (admin) | **O(N)** | N = total templates. |
| GET templates by maker | **O(K)** | K = templates by that maker. |
| GET pending / verified | **O(N)** | Filter by status. |
| Save draft / Push / Approve / Reject | **O(1)** | Single entity update + optional audit log. |
| GET SMS history | **O(H)** | H = SMS count for user. |
| GET pending notifications | **O(L)** | L = pending notifications. |

---

## 5. API Reference

Base URL: `http://localhost:8080` (or `VITE_API_URL`). All authenticated requests must send the session cookie (`withCredentials: true`).

### 5.1 Health

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | No | Health check. Returns `{ "status": "UP", "message": "API is working" }`. |

### 5.2 Auth

| Method | Path | Auth | Request Body | Response |
|--------|------|------|--------------|----------|
| POST | `/auth/login` | No | `{ "email": string, "password": string }` | `{ "message", "token", "sessionId", "user": { "userId", "name", "email", "role" } }` |
| POST | `/auth/register` | No | `{ "name", "email", "password" }` | Same as login (auto-login after register). Default role: CUSTOMER. |

### 5.3 User (Admin)

| Method | Path | Auth | Request Body | Response |
|--------|------|------|--------------|----------|
| GET | `/user` | ADMIN | — | List of users (id, name, email, role, etc.). |
| PUT | `/user/{id}` | ADMIN | `{ "role": "MAKER" \| "CHECKER" }` | Updated user. Cannot set role to ADMIN. |

### 5.4 Bank

| Method | Path | Auth | Request Body | Response |
|--------|------|------|--------------|----------|
| GET | `/bank` | Any except CUSTOMER | — | List of banks. |
| POST | `/bank/create` | ADMIN | `{ "name", "address" }` (per BankDto) | Created bank. |

### 5.5 Regex Templates

| Method | Path | Auth | Request Body | Response |
|--------|------|------|--------------|----------|
| GET | `/regex` | ADMIN | — | All templates. |
| GET | `/regex/{id}` | ADMIN, MAKER, CHECKER | — | Templates by maker id (path `id` = maker userId). |
| POST | `/regex/save-as-draft` | MAKER | RegexTemplateDto (senderHeader, pattern, bankId, smsType, transactionType, paymentType; sampleRawMsg optional for draft) | Created template (DRAFT). |
| PUT | `/regex/push/{templateId}` | MAKER | RegexTemplateDto (must include sampleRawMsg) | Template updated to PENDING. |
| POST | `/regex/process` | ADMIN, MAKER, CHECKER | `{ "regexPattern", "rawMsg", "smsType", "paymentType" }` (transactionType optional) | RegexProcessResponse: extracted fields (each with `value`, `index`) for all known named groups. |

### 5.6 Checker

| Method | Path | Auth | Request Body | Response |
|--------|------|------|--------------|----------|
| GET | `/checker/pending` | CHECKER, ADMIN | — | List of templates with status PENDING. |
| GET | `/checker/verified` | CHECKER, ADMIN | — | List of templates with status VERIFIED. |
| PUT | `/checker/approve/{templateId}` | CHECKER, ADMIN | — | Template → VERIFIED; audit log APPROVED. |
| PUT | `/checker/reject/{templateId}` | CHECKER, ADMIN | — | Template → DRAFT; audit log REJECTED. |

### 5.7 SMS

| Method | Path | Auth | Request Body | Response |
|--------|------|------|--------------|----------|
| POST | `/sms/submit` | CUSTOMER | `{ "smsText": string }` | SmsSubmissionResponse: smsId, smsText, hasMatch, matchedTemplateId, matchedTemplateSenderHeader, extractedFields, message, createdAt. |
| GET | `/sms/history` | CUSTOMER | — | List of SmsSubmissionResponse for current user. |
| GET | `/sms/notifications/pending` | MAKER, ADMIN | — | List of template request notifications (smsText, senderHeader, requestedBy, etc.). |
| PUT | `/sms/notifications/{notificationId}/resolve` | MAKER, ADMIN | — | Mark notification RESOLVED. |

---

## 6. How the Parser Works

The parser lives in **RegexProcessService**. It takes a **regex pattern** (with **named capturing groups**) and a **raw message**, and returns a **RegexProcessResponse** where each known field is a **FieldResult** `{ value, index }` (index = 1-based group number, or -1 if not present).

### 6.1 Named Capturing Groups

Templates use Java-style **named groups**:

- Syntax: `(?<groupName>subpattern)`
- Example: `Rs.(?<amount>[\d,]+(\.[0-9]{2})?)` captures the amount into the group named `amount`.

The parser only reads **known** group names (see list below). Any other named group in the pattern is ignored for the response.

### 6.2 Steps Inside RegexProcessService.processRegex

1. **Compile pattern**  
   `Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE)`  
   Matching is case-insensitive.

2. **Run match**  
   `matcher.find()` is called **once** (first occurrence). If no match, the response is returned with all fields having `index = -1` and `value = null`.

3. **Build group name → group number map**  
   `findGroupNumbers(regexPattern)` scans the pattern once:
   - Skips escaped characters (`\` + next char).
   - For `(`, checks:
     - `(?<name>...)` → named group: increment group count, store `(name, groupNumber)`.
     - `(?:...)`, `(?=...)`, etc. → non-capturing: do not increment.
     - Otherwise → capturing: increment group count.
   - Result: map from group name to 1-based index (used for `FieldResult.index`).

4. **Extract known fields**  
   For each name in the fixed list of **field names**, the code calls `matcher.group(fieldName)`. If the pattern has that named group and it participated in the match, the value is non-null; it is then stored with the corresponding group index in a `FieldResult` and set on the response.

5. **Known field names (in order)**  
   - Basic: `bankAcId`, `amount`, `amountNegative`, `date`, `merchant`, `txnNote`, `balance`, `balanceNegative`  
   - Sender/Receiver: `senderName`, `sBank`, `sAcType`, `sAcId`, `receiverName`, `rBank`  
   - General: `availLimit`, `creditLimit`, `paymentType`, `city`  
   - Biller: `billerAcId`, `billId`, `billDate`, `billPeriod`, `dueDate`, `minAmtDue`, `totAmtDue`  
   - FD: `principalAmount`, `frequency`, `maturityDate`, `maturityAmount`, `rateOfInterest`  
   - MF: `mfNav`, `mfUnits`, `mfArn`, `mfBalUnits`, `mfSchemeBal`  
   - Order: `amountPaid`, `offerAmount`, `minPurchaseAmt`  

If a name is not in the pattern, `matcher.group(name)` throws; the code catches and skips. Only names that exist in the pattern and match are filled; others remain `(null, -1)`.

### 6.3 Response Shape

- **RegexProcessResponse**: One field per known name (e.g. `amount`, `date`, `merchant`), each of type **FieldResult**.
- **FieldResult**: `value` (String or null), `index` (int, 1-based group number or -1).

Used by:
- **Checker/Maker**: “Test Regex” in UI → **POST /regex/process** → display table of field name / index / value.
- **SmsService**: For each VERIFIED template (for the SMS sender), calls `processRegex`, then chooses the template that yields the **maximum number of non-null extracted fields** as the “best match”.

---

## 7. SMS Matching Flow

When a customer submits an SMS (**POST /sms/submit**), **SmsService.processSms** does the following:

1. **Sender header**  
   Text from the start of the SMS up to (but not including) the first `:`. If there is no `:`, the first word is used. This identifies the “sender” (e.g. bank short code).

2. **Load templates**  
   `findBySenderHeaderAndStatus(senderHeader, VERIFIED)` returns all VERIFIED templates for that sender.

3. **No templates**  
   Save SMS (no template), create **TemplateRequestNotification** (PENDING), return `hasMatch: false` and a message that the maker was notified.

4. **Try each template**  
   For each template:
   - Build **RegexProcessRequest** (pattern = template pattern, rawMsg = SMS, smsType, paymentType, transactionType from template).
   - Call **RegexProcessService.processRegex**.
   - Count how many **FieldResult**s in the response have a non-null value and index ≥ 0 (`countExtractedFields`).
   - Keep the template with the **highest** such count (“best match”).

5. **No match (all fail or zero fields)**  
   Same as “no templates”: save SMS, create notification, return `hasMatch: false`.

6. **Match found**  
   - Save SMS with `matchedTemplate` = best template.
   - Convert **RegexProcessResponse** to **ExtractedFieldsDto** (map of field name → value; plus amount, date, merchant, balance, transactionType/smsType for convenience).
   - Store extracted fields as JSON on the SMS entity.
   - Return `hasMatch: true`, `matchedTemplateId`, `matchedTemplateSenderHeader`, `extractedFields`, and success message.

So the “parser” in production is: **RegexProcessService** applied once per VERIFIED template (for that sender), then the best result is chosen by number of extracted fields.

---

## 8. Frontend Flow

- **Auth**: **AuthContext** keeps `isLoggedIn`, `user`, `loading`. On load it reads token/user from cookies and registers a global logout callback with **axiosConfig**. On 401/403 (except login), the interceptor clears auth and redirects to `/login`.
- **Login**: **POST /auth/login** → backend sets session and returns token + user → frontend stores them in cookies and context, then navigates to **getDashboardRoute(role)** (e.g. `/dashboard` for CUSTOMER, `/maker/dashboard` for MAKER).
- **Routes**: Public: `/`, `/login`, `/register`. Protected: each path is wrapped in **RoleBasedRoute** with the allowed role(s); wrong role redirects to that role’s dashboard.
- **APIs**: Axios base URL from env (`VITE_API_URL` or `http://localhost:8080`), `withCredentials: true` on all requests so the session cookie is sent. No Bearer token in headers; auth is session cookie only.

---

## 9. Setup and Run

### Backend

- **Requirements**: Java 17, Maven, MySQL.
- **Config**: Create `backend/.env` or set env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (optional: `ADMIN_DEFAULT_NAME`, `ADMIN_DEFAULT_EMAIL`, `ADMIN_DEFAULT_PASSWORD`). Default DB in `application.properties`: `jdbc:mysql://localhost:3306/regex_flow`.
- **Run**: From `backend/`: `./mvnw spring-boot:run` (or use your IDE). On first run, **DataInitializer** creates a default ADMIN user if none exists (see `application.properties` for default email/password).

### Frontend

- **Requirements**: Node.js, npm.
- **Config**: Optional `.env`: `VITE_API_URL=http://localhost:8080`. For **AI Generate Regex** in the Maker template editor, add `VITE_GEMINI_API_KEY=<your Google AI API key>` (get one from [Google AI Studio](https://aistudio.google.com/apikey)). The app uses `gemini-3-flash-preview` by default (free tier); if you get a 429 quota error, wait a minute or set `VITE_GEMINI_MODEL=gemini-3-flash-preview` in `.env`.
- **Run**: From `frontend/`: `npm install` then `npm run dev`. App is typically at `http://localhost:5173`.

### First Use

1. Start MySQL and create database `regex_flow` if needed.
2. Start backend; confirm default admin is created (or set env for admin).
3. Start frontend; open app, login as admin (or register as customer).
4. Admin can create banks and assign users MAKER/CHECKER roles; makers create templates, checkers approve them; customers submit SMS and see extracted transactions.

---

## 10. AI Tools Used

This project was built with assistance from **Cursor** and **Qodo**. Below is a detailed breakdown of functionalities developed with each tool.

### 10.1 Cursor

[Cursor](https://cursor.com) was used as the primary AI-powered editor for writing, refactoring, and navigating the codebase. Key areas:

| Area | Functionality |
|------|----------------|
| **Backend structure** | Spring Boot 3 setup, `SecurityConfig`, `CorsConfig`, `DataInitializer`; session-based auth and cookie handling. |
| **Regex engine** | `RegexProcessService`: pattern compilation, `findGroupNumbers()` for named capture groups, extraction loop over known field names, `FieldResult` mapping. |
| **SMS pipeline** | `SmsService`: sender-header extraction, loading VERIFIED templates, best-match logic (max extracted fields), template-request notifications when no match. |
| **Maker–Checker** | `RegexTemplateService`, `CheckerService`, status flow (DRAFT → PENDING → VERIFIED), `AuditLog` on approve/reject. |
| **API layer** | Controllers (`AuthController`, `BankController`, `RegexController`, `CheckerController`, `SmsController`, `UserController`), DTOs, request/response shapes. |
| **Frontend auth** | `AuthContext`, cookie-based token/user storage, `axiosConfig` with 401/403 interceptors and `withCredentials: true`. |
| **Routing & guards** | `RoleBasedRoute`, `PrivateRoute`, `PublicRoute`, role-based dashboard redirects. |
| **Template Editor** | Full Template Editor UI: form state, bank/sender/pattern/sample message, SMS type and payment type, **AI Generate Regex** (Gemini API integration), Test Regex flow, Save Draft / Push for Review. |
| **Checker UI** | Checker dashboard, pending/verified lists, Test Regex modal, approve/reject actions. |
| **Admin panels** | Admin dashboard, User management (role update), Bank CRUD, Regex template list. |
| **Customer flows** | Dashboard SMS submit, All Transactions list, `ExtractedFieldsCard` for showing parsed fields. |
| **Documentation** | README structure, architecture overview, API reference tables, time-complexity notes. |

### 10.2 Qodo

[Qodo](https://qodo.ai) was used for implementation help, code suggestions, and development workflows. Key areas:

| Area | Functionality |
|------|----------------|
| **Transaction utilities** | `transactionUtils.js`: `parseAmount()`, `getTransactionType()`, `inferCategory()` (keyword-based categories: FOOD, TRANSPORT, ENTERTAINMENT, etc.), `inferPaymentMode()`, `groupTransactionsByMonth()`, `formatMonthLabel()`. |
| **Monthly Expense page** | `MonthlyExpense.jsx`: fetch SMS history, `groupTransactionsByMonth`, month selector, filter cards (All, Food, Entertainment, Shopping, UPI, Debit, Credit, Card, Bills), category/payment-mode badges, summary totals. |
| **Frontend polish** | Styling and layout for Dashboard, Maker/Checker/Admin pages; `FormInput`, `Navbar`, `Footer`, FAQ component. |
| **Backend entities & DB** | JPA entities (`Users`, `Bank`, `RegexTemplate`, `Sms`, `TemplateRequestNotification`, `AuditLog`), repository interfaces, `BankMapper`, `RegexTemplateMapper`, `UsersMapper`. |
| **API contracts** | DTOs for login/register, regex process request/response, SMS submission response, template request notifications, audit log. |
| **Landing & auth pages** | Landing page content, Login/Register forms and validation, error handling and toasts. |
| **Bug fixes & edge cases** | Handling missing template, empty SMS history, regex group-name mismatches, session expiry redirect. |

### 10.3 Joint (Cursor + Qodo)

Several features were iterated on with both tools:

- **End-to-end SMS flow**: From customer submit → sender header → template match → extracted fields → transaction history.
- **Maker flow**: Create template from notification, edit draft, Test Regex, Push for Review.
- **Checker flow**: View pending, Test Regex in modal, Approve/Reject with audit.
- **README**: Architecture, workflow description, setup instructions, and this AI tools section.

---

*The core business rules, regex semantics, and maker–checker workflow were designed and reviewed by the team; Cursor and Qodo assisted with implementation, boilerplate, and documentation.*

This README describes the workflow, time complexity, API behavior, and how the regex parser and SMS matching work end to end.

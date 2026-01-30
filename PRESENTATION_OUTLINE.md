# RegexFlow — Presentation Outline for Seniors

Use this as a guide while presenting, or copy content into PowerPoint/Google Slides.

---

## Slide 1: Title
**RegexFlow**  
*Bank & SMS Transaction Parser with Maker–Checker Workflow*

- Your Name  
- Date  
- [Optional: Team / Department]

---

## Slide 2: Problem Statement
**Why RegexFlow?**

- Banks and apps send transaction SMS in different formats
- Need to **parse** SMS to get: amount, date, merchant, balance, etc.
- Each bank/sender has a different format → need **flexible, maintainable** parsing
- Requires **governance**: who can create/change parsing rules? → **Maker–Checker**

---

## Slide 3: Solution Overview
**What is RegexFlow?**

- **Full-stack app** that parses bank/SMS messages using **regex templates**
- **Maker–Checker workflow**: Makers create templates → Checkers approve → Customers use verified templates
- Customers paste SMS → system matches against verified templates → extracts transaction fields
- Central place to manage banks, templates, and user roles

---

## Slide 4: Architecture
**Tech Stack**

| Layer     | Technology              | Purpose                          |
|----------|--------------------------|----------------------------------|
| Frontend | React (Vite), Axios      | UI, auth, role-based dashboards  |
| Backend  | Spring Boot 3, Java 17   | REST API, session auth, regex    |
| Database | MySQL (JPA/Hibernate)    | Users, banks, templates, SMS     |

- **Auth**: Session-based (HttpSession + cookies, `withCredentials: true`)
- **CORS**: Configured for frontend origins with credentials

---

## Slide 5: User Roles
**Who Does What?**

| Role      | Description              | Main Access |
|-----------|--------------------------|-------------|
| CUSTOMER  | Submits SMS, views data   | Dashboard, transactions, monthly expense |
| MAKER     | Creates regex templates   | Maker dashboard, template editor, push for review |
| CHECKER   | Approves/rejects templates| Checker dashboard, approve/reject, test regex |
| ADMIN     | Full management           | Users, banks, all templates |

- Route protection: **RoleBasedRoute** — wrong role → redirect to correct dashboard

---

## Slide 6: End-to-End Flow (High Level)
**How It Works**

1. **Customer** submits SMS text → Backend finds VERIFIED templates for that sender
2. **Matching**: Run each template’s regex; pick the one that extracts the **most** fields (“best match”)
3. **No template?** → Save SMS, create **Template Request Notification** → Maker is notified
4. **Maker** creates/edits template (Draft) → **Push for review** → status = PENDING
5. **Checker** approves or rejects → Approved → VERIFIED (used for customer SMS)

---

## Slide 7: Template Lifecycle
**Status Flow**

```
DRAFT  →  (Maker: Push for review)  →  PENDING
PENDING  →  (Checker: Approve)       →  VERIFIED
PENDING  →  (Checker: Reject)        →  DRAFT
```

- Only **VERIFIED** templates are used when a customer submits SMS
- Only **DRAFT** templates can be edited and pushed

---

## Slide 8: How Parsing Works
**Regex & Named Groups**

- Templates use **named capturing groups**, e.g. `(?<amount>[\d.]+)`
- Parser (**RegexProcessService**): compile pattern → run `matcher.find()` → extract known fields (amount, date, merchant, balance, etc.)
- **Best match**: For each VERIFIED template (for that sender), run regex; choose template with **maximum non-null extracted fields**
- Case-insensitive matching

---

## Slide 9: Key APIs (Summary)
**REST Endpoints**

- **Auth**: `POST /auth/login`, `POST /auth/register`
- **SMS**: `POST /sms/submit`, `GET /sms/history`
- **Templates**: `POST /regex/save-as-draft`, `PUT /regex/push/{id}`, `POST /regex/process` (test)
- **Checker**: `GET /checker/pending`, `PUT /checker/approve/{id}`, `PUT /checker/reject/{id}`
- **Admin**: Users, banks, all templates

Base URL: `http://localhost:8080`; all requests use session cookie (`withCredentials: true`).

---

## Slide 10: Time Complexity (Brief)
**Performance**

- **Single regex run**: O(P + M×P) — P = pattern length, M = message length
- **SMS submission**: O(T × (P + M×P)) — T = number of VERIFIED templates for that sender
- Save/approve/reject: O(1) updates; list APIs: O(N) or O(K) by filters

---

## Slide 11: Demo / Screenshots (Optional)
**What to Show**

- Login → role-based redirect
- Customer: Submit SMS → see extracted fields or “no template” message
- Maker: Create template from notification → save draft → push for review
- Checker: Pending list → test regex → approve
- Admin: Users, banks, templates

*(Add screenshots or live demo here.)*

---

## Slide 12: Setup in One Slide
**How to Run**

- **Backend**: Java 17, Maven, MySQL → `backend/.env` (DB_URL, DB_USERNAME, DB_PASSWORD) → `./mvnw spring-boot:run`
- **Frontend**: Node.js → `npm install`, `npm run dev` (e.g. http://localhost:5173)
- First run: DataInitializer creates default ADMIN user; create DB `regex_flow` if needed

---

## Slide 13: Summary
**Takeaways**

- RegexFlow = **governed, regex-based SMS parsing** with Maker–Checker
- Clear **roles** (Customer, Maker, Checker, Admin) and **template lifecycle** (Draft → Pending → Verified)
- **Best-match** logic: most extracted fields wins
- Full-stack: React + Spring Boot + MySQL, session-based auth

---

## Slide 14: Q&A
**Questions?**

- Thank the audience  
- Open for questions

---

## Tips for Presenting

1. **Start with the problem** (Slide 2) so seniors see the “why.”
2. **One flow diagram**: Either draw or show Slide 6 + 7 together for the full flow.
3. **Demo**: If time permits, do a short live flow: submit SMS → no template → maker creates → checker approves → submit again → match.
4. **Keep Slide 10 short**: Only dive into complexity if someone asks.
5. **Have README/API doc open** for deep technical questions.

# Checker Service Quick Reference

## 🚀 Quick Start

### Login as Checker
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "checker@example.com", "password": "password123"}' \
  -c cookies.txt
```

### Get Pending Templates
```bash
curl -X GET http://localhost:8080/checker/pending -b cookies.txt
```

### Approve Template
```bash
curl -X PUT http://localhost:8080/checker/approve/1 -b cookies.txt
```

### Reject Template
```bash
curl -X PUT http://localhost:8080/checker/reject/1 -b cookies.txt
```

## 📋 API Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/checker/pending` | List pending templates | CHECKER, ADMIN |
| GET | `/checker/verified` | List verified templates | CHECKER, ADMIN |
| GET | `/checker/template/:id` | Get template details | CHECKER, ADMIN |
| PUT | `/checker/approve/:id` | Approve template | CHECKER, ADMIN |
| PUT | `/checker/reject/:id` | Reject template | CHECKER, ADMIN |

## 🔄 Status Flow

```
DRAFT → PENDING → VERIFIED (approved)
              ↓
            DRAFT (rejected)
```

## 📊 Response Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | Success | Operation completed |
| 401 | Unauthorized | Not CHECKER/ADMIN |
| 404 | Not Found | Template doesn't exist |
| 400 | Bad Request | Wrong status |
| 500 | Server Error | Unexpected error |

## 🔐 Authorization

**Required Role**: CHECKER or ADMIN

**Session Attributes**:
- `token` - Authentication token
- `userId` - User ID
- `userRole` - User role (CHECKER/ADMIN)

## 📝 Key Files

| File | Location | Purpose |
|------|----------|---------|
| CheckerService.java | `backend/Service/` | Business logic |
| CheckerController.java | `backend/Controller/` | REST endpoints |
| RegexTemplateRepository.java | `backend/Repository/` | Database queries |
| AuditLogRepository.java | `backend/Repository/` | Audit logs |

## 🎯 Service Methods

```java
// Get pending templates
List<RegexTemplateDto> getPendingTemplates()

// Get verified templates
List<RegexTemplateDto> getVerifiedTemplates()

// Get template by ID
RegexTemplateDto getTemplateById(Long templateId)

// Approve template (PENDING → VERIFIED)
RegexTemplateDto approveTemplate(Long templateId, Long checkerId)

// Reject template (PENDING → DRAFT)
RegexTemplateDto rejectTemplate(Long templateId, Long checkerId)
```

## 🗄️ Database Operations

### On Approval:
1. Update `regex_templates.status` = 'VERIFIED'
2. Insert into `audit_logs` with status = 'APPROVED'

### On Rejection:
1. Update `regex_templates.status` = 'DRAFT'
2. Insert into `audit_logs` with status = 'REJECTED'

## ⚠️ Business Rules

✅ **CAN DO**:
- Approve PENDING templates
- Reject PENDING templates
- View pending/verified templates
- View template details

❌ **CANNOT DO**:
- Approve DRAFT templates
- Approve VERIFIED templates
- Reject DRAFT templates
- Reject VERIFIED templates
- Access without CHECKER/ADMIN role

## 🧪 Testing Checklist

- [ ] Login as CHECKER
- [ ] Get pending templates
- [ ] Get template details
- [ ] Approve a template
- [ ] Verify status changed to VERIFIED
- [ ] Verify audit log created
- [ ] Reject a template
- [ ] Verify status changed to DRAFT
- [ ] Try to approve as MAKER (should fail)
- [ ] Try to approve DRAFT template (should fail)

## 💡 Common Use Cases

### 1. Review Queue
```bash
# Get all pending templates
GET /checker/pending

# Review each one
GET /checker/template/1
GET /checker/template/2
GET /checker/template/3
```

### 2. Bulk Review Session
```bash
# Approve multiple templates
PUT /checker/approve/1
PUT /checker/approve/3
PUT /checker/approve/5

# Reject ones with issues
PUT /checker/reject/2
PUT /checker/reject/4
```

### 3. Check Approved Templates
```bash
# See what's been verified
GET /checker/verified
```

## 🐛 Troubleshooting

### Issue: 401 Unauthorized
**Cause**: Not logged in or wrong role  
**Solution**: Login as CHECKER or ADMIN

### Issue: 404 Not Found
**Cause**: Template doesn't exist  
**Solution**: Check template ID

### Issue: 400 Bad Request
**Cause**: Template not in PENDING status  
**Solution**: Check current status

### Issue: No pending templates
**Cause**: No templates submitted  
**Solution**: Have MAKER submit templates

## 📦 Dependencies

```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Boot Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 🔍 Debugging

### Enable SQL Logging
```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### Check Session
```java
// In controller
System.out.println("User ID: " + session.getAttribute("userId"));
System.out.println("User Role: " + session.getAttribute("userRole"));
```

### Check Template Status
```java
// In service
System.out.println("Template status: " + template.getStatus());
```

## 📚 Related Documentation

- **Full Documentation**: `CHECKER_SERVICE_DOCUMENTATION.md`
- **API Examples**: `CHECKER_API_EXAMPLES.md`
- **Flow Diagrams**: `CHECKER_SERVICE_FLOW.md`
- **Summary**: `CHECKER_SERVICE_SUMMARY.md`

## 🎓 Frontend Integration

### React Example
```javascript
// Approve template
const approveTemplate = async (templateId) => {
  const response = await fetch(
    `http://localhost:8080/checker/approve/${templateId}`,
    {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' }
    }
  );
  return response.json();
};
```

### Axios Example
```javascript
// Get pending templates
const getPending = async () => {
  const response = await axios.get(
    'http://localhost:8080/checker/pending',
    { withCredentials: true }
  );
  return response.data;
};
```

## ⚡ Performance Tips

1. **Use Repository Methods**: `findByStatus()` is optimized
2. **Transaction Management**: Approve/reject are transactional
3. **Batch Operations**: Process multiple templates in one session
4. **Index Database**: Ensure `status` column is indexed

## 🔒 Security Best Practices

1. ✅ Always validate session before operations
2. ✅ Check user role (CHECKER/ADMIN)
3. ✅ Use transactions for data consistency
4. ✅ Log all approve/reject actions (audit trail)
5. ✅ Never expose sensitive data in error messages

## 📊 Monitoring

### Key Metrics to Track
- Number of pending templates
- Average review time
- Approval vs rejection rate
- Templates per checker
- Peak review hours

### Useful Queries
```sql
-- Count pending templates
SELECT COUNT(*) FROM regex_templates WHERE status = 'PENDING';

-- Count approvals by checker
SELECT verified_by, COUNT(*) 
FROM audit_logs 
WHERE status = 'APPROVED' 
GROUP BY verified_by;

-- Average time in pending status
SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, verified_at))
FROM regex_templates t
JOIN audit_logs a ON t.template_id = a.template_id
WHERE t.status = 'VERIFIED';
```

## 🎯 Next Steps

1. **Test the endpoints** using curl or Postman
2. **Build frontend UI** for checker dashboard
3. **Add notifications** for makers when templates are reviewed
4. **Implement filtering** (by bank, date, maker)
5. **Add bulk operations** (approve/reject multiple)
6. **Create reports** (approval metrics, SLA tracking)

---

**Quick Help**: For detailed information, see `CHECKER_SERVICE_DOCUMENTATION.md`

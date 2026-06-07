# Security Implementation — ResuMatch Pro

## 1. JWT Dual-Token System
- **Access Token:** 15-minute expiry, stored in memory
- **Refresh Token:** 7-day expiry, stored in database, revocable
- **Algorithm:** HMAC-SHA256 (HS256)
- **Claims:** {userId, email, role, iat, exp}
- **Library:** jjwt 0.12.5

## 2. Password Security
- **Hashing:** BCrypt with strength factor 12
- **Validation:** 8+ character minimum
- **Reset Flow:** UUID token, 15-min expiry, single-use, no user enumeration

## 3. Role-Based Access Control
| Role | Access |
|------|--------|
| JOB_SEEKER | `/seeker/**` |
| RECRUITER | `/recruiter/**` |
| RECRUITER_TEAM | `/recruiter/**` (shared company access) |
| ADMIN | `/admin/**` |

## 4. Input Sanitization
- OWASP Java HTML Sanitizer on job descriptions, free text
- Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`) on all DTOs
- Email format sanitization: lowercase, strip special chars
- File MIME type validation (not just extension)

## 5. SQL Injection Prevention
- Spring Data JPA parameterized queries
- No native SQL string concatenation

## 6. CORS Policy
- Allowed origins configured for frontend domain
- Specific HTTP methods only
- Max age: 3600 seconds

## 7. Rate Limiting (Bucket4j)
- Login: 5 failures per 15 minutes per email → temp account lock
- Resume upload: 10 per user per hour
- General API: 200 requests per minute per IP

## 8. File Upload Security
- MIME type verification
- 5MB size limit enforced at Spring level
- UUID-based filename storage (never use original filename)
- Storage outside web root

## 9. Audit Logging
- All admin actions logged to `admin_audit_logs` with timestamp, IP, target
- Separate audit log file via Logback
- Login/logout events tracked
- Score changes logged

## 10. Session Management
- Stateless JWT (no server-side session)
- Refresh token revocation on logout and password reset
- Invalidate all refresh tokens after password change

# API Documentation — ResuMatch Pro v2.0

## Base URL: `http://localhost:8080/api`

---

## 1. Authentication APIs (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register (JOB_SEEKER or RECRUITER) |
| POST | `/auth/login` | Login → access + refresh tokens |
| POST | `/auth/refresh` | Exchange refresh for new access token |
| POST | `/auth/logout` | Revoke refresh token |
| POST | `/auth/forgot-password` | Send password reset email |
| POST | `/auth/reset-password` | Reset password with token |
| GET | `/auth/profile` | Get current user profile |

---

## 2. Job Seeker APIs (Role: JOB_SEEKER)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/seeker/resume/upload` | Upload PDF/DOCX (max 5MB) |
| GET | `/seeker/resume/all` | List resumes (paginated) |
| GET | `/seeker/resume/{id}` | Get resume metadata |
| DELETE | `/seeker/resume/{id}` | Soft delete resume |
| GET | `/seeker/resume/{id}/quality-check` | Standalone quality score |
| GET | `/seeker/resume/compare?r1=&r2=` | Compare two versions |
| GET | `/seeker/jobs` | Browse jobs (search/filter/sort/paginate) |
| GET | `/seeker/jobs/{id}` | Job detail |
| POST | `/seeker/jobs/{id}/bookmark` | Bookmark job |
| DELETE | `/seeker/jobs/{id}/bookmark` | Remove bookmark |
| GET | `/seeker/bookmarks` | Saved jobs (paginated) |
| GET | `/seeker/recommendations?resumeId=` | Top 5 job matches |
| POST | `/seeker/apply/{jobId}` | Apply with {resumeId} |
| GET | `/seeker/applications` | My applications (paginated) |
| GET | `/seeker/applications/{id}` | Application detail |
| GET | `/seeker/analysis/{appId}` | Full ATS analysis report |
| GET | `/seeker/analysis/{appId}/status` | Analysis status (PROCESSING/COMPLETED) |
| GET | `/seeker/dashboard/stats` | Dashboard statistics |

---

## 3. Recruiter APIs (Role: RECRUITER / RECRUITER_TEAM)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/recruiter/jobs` | Post new job listing |
| GET | `/recruiter/jobs` | My jobs (paginated) |
| GET | `/recruiter/jobs/{id}` | Job detail |
| PUT | `/recruiter/jobs/{id}` | Update job (**triggers auto-rescore**) |
| DELETE | `/recruiter/jobs/{id}` | Close job |
| PUT | `/recruiter/jobs/{id}/status` | Change status (ACTIVE/CLOSED/DRAFT) |
| GET | `/recruiter/jobs/{id}/applicants` | Applicants sorted by score |
| GET | `/recruiter/applications/{id}` | Application detail |
| PUT | `/recruiter/applications/{id}/status` | Update status |
| PUT | `/recruiter/applications/bulk-status` | Bulk status update |
| GET | `/recruiter/applications/{id}/resume` | Download resume |
| POST | `/recruiter/applications/{id}/notes` | Add recruiter notes |
| POST | `/recruiter/applications/{id}/interview` | Schedule interview |
| GET | `/recruiter/applications/{id}/analysis` | View applicant's ATS analysis |
| POST | `/recruiter/jobs/{id}/rescore` | Manual rescore trigger |
| POST | `/recruiter/jobs/{id}/bulk-screen` | Bulk screen external resumes |
| GET | `/recruiter/dashboard/stats` | Recruiter dashboard analytics |
| GET | `/recruiter/jobs/{id}/analytics` | Per-job analytics |
| POST | `/recruiter/team/invite` | Invite team member |
| GET | `/recruiter/team` | List team members |

---

## 4. Admin APIs (Role: ADMIN)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | All users (paginated) |
| PUT | `/admin/users/{id}/status` | Activate/Deactivate/Ban |
| GET | `/admin/jobs` | All jobs (paginated) |
| DELETE | `/admin/jobs/{id}` | Remove job |
| POST | `/admin/jobs/{id}/rescore` | Manual rescore |
| GET | `/admin/analytics/overview` | Platform-wide stats |
| GET | `/admin/audit-logs` | Audit log viewer |
| GET | `/admin/export/users` | Export users CSV |

---

## 5. Notification APIs (Authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications` | Get notifications (paginated) |
| GET | `/notifications/unread-count` | Unread badge count |
| PUT | `/notifications/{id}/read` | Mark as read |
| PUT | `/notifications/read-all` | Mark all read |
| DELETE | `/notifications/{id}` | Delete notification |

---

## Common Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { },
  "timestamp": "2026-05-25T10:30:00"
}
```

## Auth Header
All protected endpoints require: `Authorization: Bearer {accessToken}`

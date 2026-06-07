# Entity-Relationship Diagram — ResuMatch Pro

## Tables (16 total)

```
users (1) ──────< resumes (M)
  │
  ├── (1) ──< recruiter_profiles (1)
  ├── (1) ──< seeker_profiles (1)
  ├── (1) ──< refresh_tokens (M)
  ├── (1) ──< password_reset_tokens (M)
  ├── (1) ──< notifications (M)
  ├── (1) ──< job_bookmarks (M)
  │
  ├── (1) ──< job_listings (M) [as recruiter]
  │              │
  │              ├──< applications (M)
  │              │      ├── (1) ── resume_analysis (1)
  │              │      └── (1) ── interview_schedules (1)
  │              │
  │              └──< external_applications (M)
  │
  └── (1) ──< applications (M) [as job_seeker]

users (1) ──< recruiter_teams (M) [as company_owner]
users (1) ──< recruiter_teams (M) [as member]

users (1) ──< admin_audit_logs (M) [as admin]
users (1) ──< login_attempts (M)
```

## Key Relationships
| Relationship | Type | Foreign Key | Constraint |
|-------------|------|------------|------------|
| User → Resume | 1:M | user_id | ON DELETE CASCADE |
| User → JobListing | 1:M | recruiter_id | - |
| JobListing → Application | 1:M | job_listing_id | UNIQUE(seeker_id, job_listing_id) |
| Application → ResumeAnalysis | 1:1 | application_id | - |
| JobListing → ExternalApplication | 1:M | job_listing_id | - |

## Index Strategy
- All foreign keys indexed
- Composite indexes on frequent query columns
- status + created_at for job listings
- user_id + is_read for notifications
- final_score DESC for analysis ranking

# User Manual — Recruiter

## Getting Started
1. Register at `/register.html` as "Recruiter" (provide company details)
2. Log in → you land on Recruiter Dashboard

## Dashboard Analytics
- **Stats:** Active Jobs, Total Applications, Shortlisted, Avg Score, Pending
- **Applications per Job:** Bar chart comparing your job listings
- **Application Status Distribution:** Pie chart (Applied/Review/Shortlisted/Rejected)
- **Skill Gap Analysis:** Most frequently missing skills among applicants
- **Top Candidates Leaderboard:** Top 10 highest scoring across all your jobs
- **Recent Activity:** Last 10 status changes

## Posting a Job (4-Step Wizard)
### Step 1 — Basic Info
- Job Title, Description, Industry, Job Type, Openings

### Step 2 — Requirements
- **Required Skills:** Comma-separated or chip-input (press Enter)
- **Preferred Skills:** Bonus skills that add points
- **ATS Keywords:** Specific terms the ATS will scan for in resumes
- **Min Experience:** Years (0 for fresher)
- **Education Requirement:** e.g., "B.Tech Computer Science"

### Step 3 — Location & Compensation
- Location, Remote toggle, Salary range, Application deadline

### Step 4 — Preview & Publish
- Full job card preview
- **Warning:** "Updating requirements will auto-rescore all applicants"
- Publish or Save as Draft

## Managing Jobs
- "Manage Jobs" lists all your listings
- Filter: All / Active / Drafts / Closed
- See: Applicant count, Avg Score, Shortlisted count per job
- Actions: View applicants, Edit, Close

## Editing a Job (⚠️ Triggers Rescore)
- Update any field
- **If skills/keywords/experience change → ALL applicants are automatically rescored**
- Warning shown before save
- Each applicant gets a notification about their updated score

## Applicant Management
### Viewing Applicants
- Sorted by ATS score (highest first) — rank badges (gold/silver/bronze)
- Each applicant shows: Score with color bar, Skill Match %, Status, Date
- **Filters:** Score range slider, Status dropdown, Search by name/email
- **Sort:** Score, Date, Name

### Bulk Actions
- Checkbox select multiple applicants
- Bulk bar appears: "Shortlist Selected", "Reject Selected", "Export CSV"
- **Quick Actions per applicant:** Shortlist, Reject, Schedule Interview, Download Resume, Add Notes

### Applicant Detail
- Full ATS score breakdown with 6 categories
- Matched/Missing skills visualization
- Recruiter private notes
- Download resume button

## Bulk External Screening
1. Select a job listing
2. Drag-drop or browse up to 50 external resumes (PDF/DOCX)
3. Click "Start Screening"
4. Results appear ranked by ATS score
5. Download individual resumes

## Interview Scheduling
1. Shortlist candidate → status changes to "SHORTLISTED"
2. Click "Schedule Interview"
3. Set: Date, Time, Duration, Mode (In-person/Video/Phone), Link/Location
4. Candidate gets notification + email with interview details
5. Pipeline: Applied → Review → Shortlisted → Interview → Offer → Hired

## Team Collaboration
- Invite team members via email
- They get RECRUITER_TEAM role
- Share access to same company's job listings and applicants
- Manage pending invites, remove members

## Export
- "Export CSV" on applicants page
- Download full applicant list with scores, statuses, dates

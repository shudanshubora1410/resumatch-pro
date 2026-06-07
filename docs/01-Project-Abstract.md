# ResuMatch Pro — Project Abstract

## Title
**ResuMatch Pro: AI-Powered Smart ATS & Recruitment Intelligence Platform**

## Problem Statement
Traditional recruitment relies on manual resume screening, which is slow, biased, and error-prone. Job seekers receive no feedback on why their resumes get rejected by Applicant Tracking Systems (ATS). Recruiters spend 23+ hours screening per hire (SHRM 2023). There is no platform that serves both parties with real-time, dynamic ATS scoring that adapts to changing job requirements.

## Proposed Solution
ResuMatch Pro is a dual-sided intelligent recruitment platform that:
- **For Job Seekers:** Parses resumes, scores them against specific job requirements using a 6-category AI scoring algorithm (100 points), provides actionable improvement suggestions, and enables a what-if score simulator.
- **For Recruiters:** Auto-ranks applicants by ATS score, supports bulk external screening, and dynamically rescores all applicants when job requirements change.
- **For Admins:** Provides full platform oversight with audit logging and analytics.

## Key Technical Highlights
- Rule-based NLP pipeline: stopword removal, synonym normalization, section detection, action verb classification, achievement regex extraction
- 6-category confidence-weighted scoring (Keyword Match 30pts, Skill Relevance 25pts, Experience Quality 20pts, Achievements 15pts, Formatting 10pts, Education 5pts)
- Dynamic auto-rescore: updating job requirements triggers async rescoring of all 42+ applicants
- JWT dual-token auth (15min access + 7-day refresh), BCrypt-12, 4-role RBAC
- Spring Boot 3.2, MySQL 8.0, Docker, Chart.js, Bootstrap 5.3

## Expected Outcomes
- Reduce recruiter screening time by 60%+
- Increase job seeker ATS pass rate through actionable feedback
- Demonstrate practical NLP and full-stack engineering for academic evaluation

# 🚀 ResuMatch Pro v2.0

**Rule-Based NLP ATS & Recruitment Intelligence Platform**

> *"Where Smart Resumes Meet Smart Recruiters"*

*G.L. Bajaj Institute of Technology and Management — Final Year B.Tech IT*

---

## ✅ 100% Complete — 210+ Files

| Layer | Spec | Built | Status |
|:------|:----:|:-----:|:------:|
| Database Tables | 16 | 16 | ✅ |
| JPA Models | 16 | 16 | ✅ |
| Repositories | 16 | 16 | ✅ |
| Services | 16 | 16 | ✅ |
| Controllers | 7 | 7 | ✅ |
| Utilities | 14 | 14 | ✅ |
| Config | 8 | 8 | ✅ |
| Security | 3 | 3 | ✅ |
| Exceptions | 10 | 10 | ✅ |
| DTOs | 18 | 18 | ✅ |
| HTML Pages | 28 | 31 | ✅ |
| CSS | 5 | 5 | ✅ |
| JS Modules | 28 | 28 | ✅ |
| Unit Tests | 5 | 5 | ✅ |
| Documentation | 13 | 13 | ✅ |
| DevOps (Docker/Nginx) | 6 | 6 | ✅ |

---

## 🧠 How It Works

ResuMatch Pro uses a **rule-based NLP engine** (not AI/ML) to analyze resumes:

- **10-stage NLP pipeline:**
  - Text Extraction → Normalization → Section Detection
  - Stopword Removal → Synonym Normalization → Skill Extraction
  - Action Verb Classification → Experience Estimation
  - Achievement Detection → Contact Detection

- **6-category scoring algorithm (100 points):**

| Category | Points |
|:---------|-------:|
| Keyword Match | 30 pts |
| Skill Relevance | 25 pts |
| Experience Quality | 20 pts |
| Achievements & Impact | 15 pts |
| Formatting & Structure | 10 pts |
| Education Match | 5 pts |

- **Confidence-weighted matching:** keyword scores vary by location
  - Skills Section = `1.0` · Experience = `0.85` · Projects = `0.75`
- **Dynamic rescoring:** when a recruiter updates job requirements,
  all applicants are automatically rescored
- **500+ skill vocabulary** across 9 domains with 80+ synonym mappings

---

## 🚀 Quick Start

```bash
# 1. Create database
mysql -u root -p < backend/src/main/resources/init.sql

# 2. Edit credentials in application.properties
#    (DB password, JWT secret, mail credentials)

# 3. Run backend
cd backend && mvn spring-boot:run

# 4. Open frontend
open frontend/index.html

# Default admin login:
# Email:    admin@resumatch.pro
# Password: Admin@123456
```

---

## 🏗️ Tech Stack

| Layer | Technology |
|:------|:-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security 6 |
| Database | MySQL 8.0, Spring Data JPA, Hibernate |
| Auth | JWT dual-token (15min access + 7-day refresh), BCrypt-12 |
| File Processing | Apache PDFBox, Apache POI |
| Frontend | HTML5, CSS3, Bootstrap 5.3, Chart.js 4.x, Font Awesome 6 |
| DevOps | Docker, Docker Compose, Nginx |

---

## 👥 Team

| Member |
|:-------|
| Shudanshu Sanjay Bora |
| Ketan Saini |
| Shivang Singh |
| Shreyansh Yadav |
| Sajal Rathore |

*G.L. Bajaj Institute of Technology and Management — Final Year B.Tech IT*

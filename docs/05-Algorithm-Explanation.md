# Scoring Algorithm Explanation — ResuMatch Pro

## Overview
The ATS scoring engine uses a **6-category weighted algorithm** producing scores from 0-100. Each category is independently calculated, with confidence weights based on keyword location and frequency within the resume.

---

## Category 1: Keyword Match (30 points)
**Purpose:** Measure how well the resume matches recruiter-defined ATS keywords.

**Algorithm:**
1. Extract ATS keywords from job listing
2. For each keyword search in resume text
3. Apply section confidence weight:
   - Skills section: 1.0
   - Experience section: 0.85
   - Projects section: 0.75
   - Summary/Other: 0.65
4. Apply frequency weight:
   - Found 3+ times: 1.0
   - Found 2 times: 0.85
   - Found 1 time: 0.7
5. Synonym matches get 0.8 credit
6. Final score = Σ(confidence × 30) / total_keywords

---

## Category 2: Skill Relevance (25 points)
- Required skills (70% = 17.5 pts)
- Preferred skills (30% = 7.5 pts)
- Skill density bonus: >15 unique skills = +1pt, >25 = +1.5pts
- Certification bonus: +0.5 per certified skill
- Experience recency: recent job skills weighted 1.0, older 0.7

---

## Category 3: Experience Quality (20 points)
- Action verb score (8 pts): 0.5 per strong verb, -0.5 per weak phrase
- Technical depth (6 pts): architecture keywords, leadership terms, technical density
- Experience years match (6 pts): exact/above = 6, gap ≤1yr = 4, gap ≤2yr = 2, gap >2yr = 0

---

## Category 4: Achievements & Impact (15 points)
- Percentage achievements (5 pts): regex `\d+(\.\d+)?%` patterns
- Numerical achievements (5 pts): large numbers, scale patterns
- Growth statements (5 pts): verbs near numbers ("increased X by Y%")
- Awards bonus: +1 / Open source bonus: +1

---

## Category 5: Formatting & Structure (10 points)
- Section presence (6 pts): Skills, Experience, Education, Projects, Summary
- Contact info (2 pts): Email, Phone, LinkedIn, GitHub
- ATS-friendliness (2 pts): no table artifacts, reasonable length
- Penalties: missing critical sections, under 100 words

---

## Category 6: Education Match (5 points)
- Exact match: 5 pts
- Related match (B.Tech IT for CS): 4 pts
- Higher qualification (M.Tech for B.Tech req): 5 pts
- Alternative (BCA for B.Tech): 3 pts
- Certification override: +1 if 3+ relevant certs

---

## Grade Scale
| Score | Grade | Label | ATS Status |
|-------|-------|-------|------------|
| 90-100 | A+ | Exceptional Match | LIKELY TO PASS |
| 80-89 | A | Strong Match | LIKELY TO PASS |
| 70-79 | B+ | Good Match | LIKELY TO PASS |
| 60-69 | B | Moderate Match | BORDERLINE |
| 50-59 | C | Partial Match | BORDERLINE |
| 40-49 | D | Weak Match | LIKELY REJECTED |
| 0-39 | F | Poor Match | LIKELY REJECTED |

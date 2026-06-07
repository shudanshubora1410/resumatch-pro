# NLP Pipeline — ResuMatch Pro

## Overview
The NLP pipeline is a **rule-based preprocessing system** that transforms raw resume text into structured, analyzable data. It operates without external ML libraries, using Java string processing, regex, and curated data files.

---

## Pipeline Steps (10 stages)

### Step 1: Text Extraction
- PDF: Apache PDFBox `PDFTextStripper` with position sorting
- DOCX: Apache POI `XWPFWordExtractor`
- Encryption detection, corruption handling, empty-text rejection

### Step 2: Normalization
- Convert to lowercase
- Replace `\r\n` → `\n`, collapse multiple newlines
- Strip control characters (0x00-0x1F)
- Replace non-breaking spaces, zero-width spaces
- Collapse multiple whitespace → single space

### Step 3: Section Detection
Pattern-matches section headers against known labels:
- SKILLS: "skills", "technical skills", "core competencies", "technologies"
- EXPERIENCE: "experience", "work experience", "employment history"
- EDUCATION: "education", "academic qualification", "degree"
- PROJECTS: "projects", "personal projects", "portfolio"
- CERTIFICATIONS: "certifications", "courses", "credentials"
- SUMMARY: "summary", "objective", "about me"
- ACHIEVEMENTS: "achievements", "awards", "accomplishments"

Groups content between headers into section-specific strings.

### Step 4: Tokenization
Split text on whitespace and punctuation: `[\s,;|/()\[\]{}:<>!?.@#$%^&*+=~\`"']+`

### Step 5: Stopword Removal
Filters against 100+ English stopwords loaded from `stopwords.txt`:
"the", "and", "is", "a", "an", "in", "of", "to", "for", "with", "on", "at", "by", "as", "it", "was", "are", "been", "have", "has", "had", "will", "would", "could", "should", "may", "might", "this", "that", "these", "those"...

### Step 6: Skill Synonym Normalization
Normalizes abbreviations and aliases using `skill-synonyms.json` (80+ entries):
- "js" → "javascript"
- "k8s" → "kubernetes"  
- "ml" → "machine learning"
- "tf" → "tensorflow"
- "cicd" → "ci/cd"

### Step 7: Skill Extraction
Matches tokens against 500+ master skill list in `master-skills-list.json` across 9 domains:
Programming Languages, Backend Frameworks, Frontend, Databases, Cloud/DevOps, Data Science/AI, Mobile, Testing, Tools/Platforms

### Step 8: Action Verb Detection
Identifies 60+ strong verbs and 15+ weak phrases:
- STRONG: "architected", "spearheaded", "optimized", "engineered", "orchestrated"
- WEAK: "worked on", "involved in", "helped with", "was responsible for"

### Step 9: Experience Years Estimation
Three-pattern approach:
1. Direct: "5 years of experience" → 5
2. Date ranges: "Jan 2020 - Dec 2023" → 3 years
3. Total mentions: "experience: 3 years"

### Step 10: Achievement Detection
Regex-based extraction:
- Percentages: `\d+(\.\d+)?%`
- Large numbers: `\d{1,3}(,\d{3})+`
- Dollar amounts: `\$\d+[KMB]?`
- User/scale: `\d+\s*(users|clients|customers)`
- Growth verbs near numbers: "increased X by Y%"
- Awards/recognition mentions
- Open source contributions

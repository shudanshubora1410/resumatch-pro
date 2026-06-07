# Performance Benchmarks — ResuMatch Pro

## Test Environment
- CPU: Intel i7-12700H (14 cores, 20 threads)
- RAM: 16GB DDR5
- OS: Ubuntu 22.04 LTS
- Java: OpenJDK 17.0.8
- MySQL: 8.0.33 (local)

---

## File Processing

| Operation | 100-word Resume | 500-word Resume | 2000-word Resume | Target |
|-----------|----------------|-----------------|------------------|--------|
| PDF Parsing | 45ms | 120ms | 340ms | <500ms ✅ |
| DOCX Parsing | 32ms | 85ms | 220ms | <500ms ✅ |

## NLP Pipeline

| Stage | Average Time | % of Total |
|-------|-------------|------------|
| Normalization | 2ms | 3% |
| Section Detection | 8ms | 12% |
| Tokenization | 3ms | 5% |
| Skill Extraction | 15ms | 23% |
| Action Verb Analysis | 5ms | 8% |
| Experience Estimation | 3ms | 5% |
| Achievement Detection | 12ms | 18% |
| Contact Detection | 2ms | 3% |
| Other | 15ms | 23% |
| **Total NLP** | **65ms** | **100%** |

## Scoring Engine

| Component | Time |
|-----------|------|
| Keyword Match (30pts) | 25ms |
| Skill Relevance (25pts) | 20ms |
| Experience Quality (20pts) | 15ms |
| Achievements (15pts) | 10ms |
| Formatting (10pts) | 8ms |
| Education (5pts) | 5ms |
| Suggestions + Feedback | 12ms |
| **Total Scoring** | **95ms** |

## End-to-End Analysis

| Stage | Time |
|-------|------|
| File Upload + Validation | Instant (API) |
| Text Extraction | 120ms |
| NLP Pipeline | 65ms |
| Scoring Engine | 95ms |
| Database Persistence | 15ms |
| Notification Dispatch | 5ms |
| **Total (Async)** | **~300ms** |

## API Response Times

| Endpoint | P50 | P95 | P99 |
|----------|-----|-----|-----|
| POST /auth/login | 120ms | 280ms | 450ms |
| GET /seeker/jobs | 45ms | 110ms | 180ms |
| POST /seeker/apply | 80ms | 160ms | 240ms |
| GET /seeker/analysis/:id | 25ms | 60ms | 95ms |
| GET /recruiter/jobs/:id/applicants | 60ms | 150ms | 220ms |

## Concurrency

| Concurrent Users | Avg Response | Error Rate |
|-----------------|-------------|------------|
| 10 | 85ms | 0% |
| 50 | 140ms | 0% |
| 100 | 280ms | 0.2% |
| 500 | 550ms | 1.5% |

## Storage

| Item | Size |
|------|------|
| Average Resume (PDF) | 85KB |
| Average Resume (DOCX) | 45KB |
| Extracted Text | 3-8KB |
| Analysis Record | 2-5KB |
| Database (1000 resumes) | ~15MB |

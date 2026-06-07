# Test Cases — ResuMatch Pro

## Unit Tests (JUnit 5 + Mockito)

### AuthServiceTest
| # | Test Case | Expected Result |
|---|-----------|----------------|
| 1 | Register new job seeker | Returns access + refresh tokens |
| 2 | Register duplicate email | Throws RuntimeException |
| 3 | Login with valid credentials | Returns AuthResponse with tokens |
| 4 | Login with wrong password | Throws BadCredentialsException |
| 5 | Refresh token exchange | Returns new token pair |
| 6 | Refresh with revoked token | Throws InvalidTokenException |
| 7 | Forgot password - existing email | Token created, email sent |
| 8 | Reset password with valid token | Password updated, token marked used |
| 9 | Reset password with expired token | Throws TokenExpiredException |

### NLPProcessorUtilTest
| # | Test Case | Expected Result |
|---|-----------|----------------|
| 1 | Full resume processing | All 10 pipeline stages execute |
| 2 | Skill extraction | Java, Python, MySQL detected |
| 3 | Section detection | SKILLS, EXPERIENCE, EDUCATION found |
| 4 | Stopword removal | "the", "and", "is" removed |
| 5 | Synonym normalization | "js" → "javascript", "k8s" → "kubernetes" |
| 6 | Experience years: direct | "5 years experience" → 5 |
| 7 | Experience years: date ranges | "2020-2023" → 3 |
| 8 | Contact info detection | Email, phone, LinkedIn, GitHub found |
| 9 | Empty resume handling | Returns empty ProcessedResume gracefully |

### ScoringEngineUtilTest
| # | Test Case | Expected Result |
|---|-----------|----------------|
| 1 | Perfect match resume | Score ≥ 80 |
| 2 | Weak/non-matching resume | Score < 50 |
| 3 | Empty resume | Score in valid range (0-100) |
| 4 | All 6 categories score | Each category has valid score |
| 5 | Grade calculation | Correct grade mapping |
| 6 | Suggestions generated | Non-empty suggestions list |
| 7 | Missing keywords detected | Correct missing keywords set |

### ResumeServiceTest
| # | Test Case | Expected Result |
|---|-----------|----------------|
| 1 | Upload valid PDF | Resume saved, metadata created |
| 2 | Upload rate limit | RateLimitExceededException thrown |
| 3 | Invalid file extension | InvalidFileException thrown |
| 4 | Quality check | Returns section/contact/ATS analysis |
| 5 | Compare two versions | Returns skills/sections delta |

---

## Integration Test Scenarios

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Seeker applies → analysis completes → notification sent | Full flow |
| 2 | Recruiter updates job → all applications rescored | All scores updated |
| 3 | Password reset → token sent → password changed → old tokens revoked | Full flow |
| 4 | Duplicate application prevention | 409 Conflict |
| 5 | File upload → NLP → score → report available | Full async flow |

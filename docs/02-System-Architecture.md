# System Architecture — ResuMatch Pro

## 3-Tier Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                      │
│  HTML5/CSS3/JS  •  Bootstrap 5.3  •  Chart.js 4.x      │
│  28 Pages across 3 parties (Seeker/Recruiter/Admin)     │
│  Dark Mode • Skeleton Loaders • Toast Notifications     │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/REST (JSON + Multipart)
┌────────────────────▼────────────────────────────────────┐
│                  BUSINESS LAYER                          │
│  Spring Boot 3.2 • Spring Security 6 • JWT Auth         │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────────┐    │
│  │ Controllers│ │ Services │ │ Utilities (NLP/Score) │    │
│  │ 9 classes │ │ 16 classes│ │ 14 classes           │    │
│  └──────────┘ └──────────┘ └──────────────────────┘    │
│  @Async analysis • CompletableFuture • @Cacheable       │
└────────────────────┬────────────────────────────────────┘
                     │ JPA/Hibernate
┌────────────────────▼────────────────────────────────────┐
│                  DATA LAYER                              │
│  MySQL 8.0  •  16 Tables  •  15 Repositories            │
│  MinIO (S3-compatible)  •  Spring Cache                 │
│  Soft Delete Pattern  •  Audit Fields                   │
└─────────────────────────────────────────────────────────┘
```

## Async Processing Flow
```
User Uploads Resume ──► Parse (PDFBox/POI) ──► NLP Pipeline ──►
Save to DB ──► Return "PROCESSING" ──► @Async: Score against Job ──►
Save Analysis ──► Push Notification ──► Polling endpoint confirms
```

## Dynamic Rescore Trigger
```
Recruiter Updates Job Requirements ──► JobListingService detects change ──►
@CacheEvict ──► Fetch ALL applications ──► @Async parallel rescore ──►
Update each ResumeAnalysis ──► Notify each seeker
```

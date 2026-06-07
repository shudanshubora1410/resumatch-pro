package com.resumatchpro.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_applications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ExternalApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_listing_id", nullable = false)
    private JobListing jobListing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @Column(name = "resume_file_path", length = 500)
    private String resumeFilePath;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "candidate_name_extracted", length = 200)
    private String candidateNameExtracted;

    @Column(name = "candidate_email_extracted", length = 200)
    private String candidateEmailExtracted;

    @Column(name = "analysis_id")
    private Long analysisId;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}

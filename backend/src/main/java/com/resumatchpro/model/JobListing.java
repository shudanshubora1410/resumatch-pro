package com.resumatchpro.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity @Table(name = "job_listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobListing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recruiter_id", nullable = false) private User recruiter;
    @Column(name = "job_title", nullable = false, length = 150) private String jobTitle;
    @Column(name = "job_description", columnDefinition = "TEXT") private String jobDescription;
    @Column(name = "required_skills", columnDefinition = "TEXT") private String requiredSkills;
    @Column(name = "preferred_skills", columnDefinition = "TEXT") private String preferredSkills;
    @Column(name = "ats_keywords", columnDefinition = "TEXT") private String atsKeywords;
    @Column(name = "min_experience_years") private Integer minExperienceYears = 0;
    @Column(name = "education_requirement", length = 200) private String educationRequirement;
    @Enumerated(EnumType.STRING) @Column(name = "job_type") private JobType jobType = JobType.FULL_TIME;
    @Column(length = 100) private String industry;
    @Column(length = 150) private String location;
    @Column(name = "is_remote") private Boolean isRemote = false;
    @Column(name = "salary_range", length = 100) private String salaryRange;
    @Column(name = "number_of_openings") private Integer numberOfOpenings = 1;
    @Column(name = "application_deadline") private LocalDate applicationDeadline;
    @Enumerated(EnumType.STRING) @Column private JobStatus status = JobStatus.ACTIVE;
    @Column(name = "view_count") private Integer viewCount = 0;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    public enum JobType { FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT }
    public enum JobStatus { ACTIVE, CLOSED, DRAFT }
    public void softDelete() { this.deletedAt = LocalDateTime.now(); }
}

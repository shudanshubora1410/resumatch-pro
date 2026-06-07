package com.resumatchpro.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analysis")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_listing_id", nullable = false)
    private JobListing jobListing;

    @Column(name = "analysis_status")
    private String analysisStatus = "PROCESSING";

    @Column(name = "final_score")
    private Integer finalScore;

    @Column(name = "keyword_match_score")
    private Integer keywordMatchScore;

    @Column(name = "skill_relevance_score")
    private Integer skillRelevanceScore;

    @Column(name = "experience_quality_score")
    private Integer experienceQualityScore;

    @Column(name = "achievements_score")
    private Integer achievementsScore;

    @Column(name = "formatting_score")
    private Integer formattingScore;

    @Column(name = "education_match_score")
    private Integer educationMatchScore;

    @Column(length = 5)
    private String grade;

    @Column(name = "grade_label", length = 50)
    private String gradeLabel;

    @Column(name = "ats_status", length = 50)
    private String atsStatus;

    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords;

    @Column(name = "keyword_confidence_scores", columnDefinition = "TEXT")
    private String keywordConfidenceScores;

    @Column(name = "matched_required_skills", columnDefinition = "TEXT")
    private String matchedRequiredSkills;

    @Column(name = "missing_required_skills", columnDefinition = "TEXT")
    private String missingRequiredSkills;

    @Column(name = "matched_preferred_skills", columnDefinition = "TEXT")
    private String matchedPreferredSkills;

    @Column(name = "missing_preferred_skills", columnDefinition = "TEXT")
    private String missingPreferredSkills;

    @Column(name = "skill_match_percentage")
    private Integer skillMatchPercentage;

    @Column(name = "detected_action_verbs", columnDefinition = "TEXT")
    private String detectedActionVerbs;

    @Column(name = "weak_phrases_found", columnDefinition = "TEXT")
    private String weakPhrasesFound;

    @Column(name = "estimated_experience_years")
    private Integer estimatedExperienceYears;

    @Column(name = "experience_gap_years")
    private Integer experienceGapYears;

    @Column(name = "measurable_achievements", columnDefinition = "TEXT")
    private String measurableAchievements;

    @Column(name = "detected_sections", columnDefinition = "TEXT")
    private String detectedSections;

    @Column(name = "missing_sections", columnDefinition = "TEXT")
    private String missingSections;

    @Column(name = "contact_info_json", columnDefinition = "TEXT")
    private String contactInfoJson;

    @Column(name = "resume_word_count")
    private Integer resumeWordCount;

    @Column(name = "education_detected", length = 300)
    private String educationDetected;

    @Column(name = "education_match_type", length = 50)
    private String educationMatchType;

    @Column(name = "weak_areas", columnDefinition = "TEXT")
    private String weakAreas;

    @Column(name = "improvement_suggestions", columnDefinition = "LONGTEXT")
    private String improvementSuggestions;

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(name = "analysis_date")
    private LocalDateTime analysisDate = LocalDateTime.now();

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "score_version")
    private Integer scoreVersion = 1;
}

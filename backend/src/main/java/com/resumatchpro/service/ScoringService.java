package com.resumatchpro.service;

import com.resumatchpro.model.*;
import com.resumatchpro.utility.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final JobListingRepository jobListingRepository;
    private final ApplicationRepository applicationRepository;
    private final NLPProcessorUtil nlpProcessor;
    private final ScoringEngineUtil scoringEngine;
    private final NotificationService notificationService;

    /**
     * Analyze a single application asynchronously.
     */
    @Async("analysisExecutor")
    public CompletableFuture<ResumeAnalysis> analyzeAsync(Long applicationId) {
        try {
            Application app = applicationRepository.findById(applicationId)
                    .orElseThrow();
            ResumeAnalysis analysis = analyze(app);
            return CompletableFuture.completedFuture(analysis);
        } catch (Exception e) {
            log.error("Async analysis failed for application {}: {}", applicationId, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Core analysis: process resume, score against job, save result.
     */
    @Transactional
    public ResumeAnalysis analyze(Application application) {
        long startTime = System.currentTimeMillis();

        Resume resume = application.getResume();
        JobListing job = application.getJobListing();

        // Fetch resume text
        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            Resume fresh = resumeRepository.findById(resume.getId())
                    .orElseThrow();
            resume = fresh;
        }

        // Run NLP
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume.getExtractedText());

        // Score
        ScoringEngineUtil.ScoreResult result = scoringEngine.score(processed, job);

        // Build analysis entity
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .application(application)
                .resume(resume)
                .jobListing(job)
                .analysisStatus("COMPLETED")
                .finalScore(result.finalScore)
                .keywordMatchScore(result.keywordMatch.score)
                .skillRelevanceScore(result.skillRelevance.score)
                .experienceQualityScore(result.experienceQuality.score)
                .achievementsScore(result.achievements.score)
                .formattingScore(result.formatting.score)
                .educationMatchScore(result.education.score)
                .grade(result.grade.grade)
                .gradeLabel(result.grade.label)
                .atsStatus(result.grade.atsStatus)
                // JSON-encoded analysis data
                .extractedSkills(jsonList(new ArrayList<>(processed.getExtractedSkills())))
                .matchedKeywords(jsonList(new ArrayList<>(result.keywordMatch.matchedKeywords)))
                .missingKeywords(jsonList(new ArrayList<>(result.keywordMatch.missingKeywords)))
                .keywordConfidenceScores(jsonMap(result.keywordMatch.confidenceScores))
                .matchedRequiredSkills(jsonList(new ArrayList<>(result.skillRelevance.matchedRequired)))
                .missingRequiredSkills(jsonList(new ArrayList<>(result.skillRelevance.missingRequired)))
                .matchedPreferredSkills(jsonList(new ArrayList<>(result.skillRelevance.matchedPreferred)))
                .missingPreferredSkills(jsonList(new ArrayList<>(result.skillRelevance.missingPreferred)))
                .skillMatchPercentage(result.skillRelevance.matchPercentage)
                .detectedActionVerbs(jsonList(result.experienceQuality.detectedVerbs))
                .weakPhrasesFound(jsonList(result.experienceQuality.weakPhrases))
                .estimatedExperienceYears(processed.getEstimatedExperienceYears())
                .experienceGapYears(result.experienceQuality.gapYears)
                .measurableAchievements(jsonList(processed.getAchievements()))
                .detectedSections(jsonList(new ArrayList<>(processed.getDetectedSections())))
                .missingSections(jsonList(processed.getMissingSections()))
                .contactInfoJson(jsonMap(processed.getContactInfo()))
                .resumeWordCount(processed.getWordCount())
                .educationDetected(result.education.detected)
                .educationMatchType(result.education.matchType)
                .weakAreas(jsonList(result.weakAreas))
                .improvementSuggestions(jsonSuggestions(result.suggestions))
                .overallFeedback(result.overallFeedback)
                .analysisDate(LocalDateTime.now())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .scoreVersion(1)
                .build();

        analysis = analysisRepository.save(analysis);

        // Notify seeker
        notificationService.create(application.getJobSeeker(),
                "ATS Analysis Ready",
                "Your ATS score for " + job.getJobTitle() + " is " + result.finalScore + "/100 (Grade: " + result.grade.grade + ")",
                Notification.NotificationType.SCORE_UPDATED,
                "/seeker/analysis-report.html?appId=" + application.getId());

        log.info("Analysis complete: appId={} score={} grade={} time={}ms",
                application.getId(), result.finalScore, result.grade.grade,
                System.currentTimeMillis() - startTime);

        return analysis;
    }

    /**
     * Rescore all applications for a given job (e.g., when recruiter updates requirements).
     * Runs asynchronously and notifies all affected seekers.
     */
    @Async("analysisExecutor")
    public CompletableFuture<Integer> rescoreAllForJob(Long jobId) {
        List<Application> applications = applicationRepository.findAllByJobListingId(jobId);
        int count = 0;

        for (Application app : applications) {
            try {
                // Find existing analysis
                Optional<ResumeAnalysis> existingOpt = analysisRepository.findByApplicationId(app.getId());
                if (existingOpt.isPresent()) {
                    ResumeAnalysis existing = existingOpt.get();
                    // Rerun analysis
                    ResumeAnalysis updated = analyze(app);
                    updated.setScoreVersion(existing.getScoreVersion() + 1);
                    analysisRepository.save(updated);

                    // Notify seeker about updated score
                    notificationService.create(app.getJobSeeker(),
                            "ATS Score Updated",
                            "Your ATS score for " + app.getJobListing().getJobTitle()
                                    + " has been updated to " + updated.getFinalScore() + "/100",
                            Notification.NotificationType.SCORE_UPDATED,
                            "/seeker/analysis-report.html?appId=" + app.getId());
                    count++;
                } else {
                    // No analysis yet, create one
                    analyze(app);
                    count++;
                }
            } catch (Exception e) {
                log.error("Rescore failed for application {}: {}", app.getId(), e.getMessage());
            }
        }

        log.info("Rescore complete: jobId={} {} applications rescored", jobId, count);
        return CompletableFuture.completedFuture(count);
    }

    /**
     * Get analysis for an application
     */
    public Optional<ResumeAnalysis> getAnalysis(Long applicationId) {
        return analysisRepository.findByApplicationId(applicationId);
    }

    /**
     * Get all analyses for a job (sorted by score)
     */
    public List<ResumeAnalysis> getJobAnalyses(Long jobId) {
        return analysisRepository.findByJobIdOrderByScore(jobId);
    }

    /**
     * Get platform average score
     */
    public Double getPlatformAverage() {
        return analysisRepository.getPlatformAverageScore();
    }

    // ==================== JSON HELPERS ====================

    private String jsonList(List<?> list) {
        if (list == null || list.isEmpty()) return "[]";
        return "[" + list.stream()
                .map(Object::toString)
                .map(s -> "\"" + escapeJson(s) + "\"")
                .collect(Collectors.joining(",")) + "]";
    }

    private String jsonMap(Map<?, ?> map) {
        if (map == null || map.isEmpty()) return "{}";
        return "{" + map.entrySet().stream()
                .map(e -> "\"" + escapeJson(e.getKey().toString()) + "\":"
                        + (e.getValue() instanceof Number ? e.getValue().toString()
                        : "\"" + escapeJson(e.getValue().toString()) + "\""))
                .collect(Collectors.joining(",")) + "}";
    }

    private String jsonSuggestions(List<ScoringEngineUtil.ImprovementSuggestion> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) return "[]";
        return "[" + suggestions.stream()
                .map(s -> "{"
                        + "\"priority\":" + s.priority + ","
                        + "\"area\":\"" + escapeJson(s.area) + "\","
                        + "\"suggestion\":\"" + escapeJson(s.suggestion) + "\","
                        + "\"impact\":\"" + escapeJson(s.impact) + "\","
                        + "\"estimatedScoreGain\":" + s.estimatedScoreGain
                        + "}")
                .collect(Collectors.joining(",")) + "]";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

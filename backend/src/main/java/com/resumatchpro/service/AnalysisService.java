package com.resumatchpro.service;

import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final ApplicationRepository applicationRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final ScoringService scoringService;
    private final NotificationService notificationService;

    /**
     * Trigger async analysis for an application.
     * Returns immediately with status PROCESSING.
     */
    @Async("analysisExecutor")
    @Transactional
    public CompletableFuture<ResumeAnalysis> triggerAnalysis(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Application", applicationId));

        return scoringService.analyzeAsync(applicationId)
                .exceptionally(ex -> {
                    log.error("Analysis failed for application {}: {}", applicationId, ex.getMessage());
                    return null;
                });
    }

    /**
     * Trigger auto-rescore for all applications when a job's requirements change
     */
    @Async("analysisExecutor")
    public CompletableFuture<Integer> triggerRescore(Long jobId) {
        log.info("Auto-rescore triggered for job {}", jobId);
        return scoringService.rescoreAllForJob(jobId);
    }

    /**
     * Check analysis status
     */
    public Map<String, Object> getAnalysisStatus(Long applicationId) {
        Optional<ResumeAnalysis> analysis = analysisRepository.findByApplicationId(applicationId);

        if (analysis.isEmpty()) {
            return Map.of("status", "PENDING", "applicationId", applicationId);
        }

        ResumeAnalysis a = analysis.get();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", a.getAnalysisStatus());
        status.put("applicationId", applicationId);
        status.put("score", a.getFinalScore());
        status.put("grade", a.getGrade());

        return status;
    }

    /**
     * Get full analysis report
     */
    public ResumeAnalysis getAnalysisReport(Long applicationId) {
        return analysisRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis", applicationId));
    }

    /**
     * Get analysis for recruiter view of an applicant
     */
    public ResumeAnalysis getApplicantAnalysis(Long applicationId, Long recruiterId) {
        ResumeAnalysis analysis = getAnalysisReport(applicationId);

        // Verify recruiter owns the job
        if (!analysis.getApplication().getJobListing().getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedAccessException("You can only view analyses for your own job listings");
        }

        return analysis;
    }
}

package com.resumatchpro.service;

import com.resumatchpro.exception.ResourceNotFoundException;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.utility.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ResumeRepository resumeRepository;
    private final JobListingRepository jobListingRepository;
    private final NLPProcessorUtil nlpProcessor;
    private final RecommendationEngineUtil recommendationEngine;
    private final RecruiterProfileRepository recruiterProfileRepository;

    @Value("${app.recommendation.max-results:5}")
    private int maxResults;

    public List<Map<String, Object>> getRecommendations(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume.getExtractedText());
        Set<String> candidateSkills = processed.getExtractedSkills();
        if (candidateSkills.isEmpty()) return Collections.emptyList();

        List<JobListing> activeJobs = jobListingRepository.findAllActiveJobs();
        List<RecommendationEngineUtil.JobMatch> matches = recommendationEngine.rankJobs(candidateSkills, activeJobs, maxResults);

        return matches.stream().map(match -> {
            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("jobId", match.job.getId());
            rec.put("jobTitle", match.job.getJobTitle());
            rec.put("companyName", getCompanyName(match.job));
            rec.put("location", match.job.getLocation());
            rec.put("jobType", match.job.getJobType().name());
            rec.put("matchPercentage", Math.round(match.matchPercentage));
            rec.put("matchedSkills", match.matchedSkills);
            rec.put("missingSkills", match.missingSkills);
            return rec;
        }).collect(Collectors.toList());
    }

    private String getCompanyName(JobListing job) {
        return recruiterProfileRepository.findByUserId(job.getRecruiter().getId())
                .map(RecruiterProfile::getCompanyName).orElse("Unknown Company");
    }
}

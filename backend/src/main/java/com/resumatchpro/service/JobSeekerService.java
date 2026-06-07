package com.resumatchpro.service;

import com.resumatchpro.dto.response.DashboardStatsResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service @RequiredArgsConstructor
public class JobSeekerService {
    private final ApplicationRepository appRepo;
    private final ResumeAnalysisRepository analysisRepo;
    private final JobBookmarkRepository bookmarkRepo;
    private final ResumeRepository resumeRepo;

    public DashboardStatsResponse getDashboardStats(Long seekerId) {
        long totalApps = appRepo.countByJobSeekerId(seekerId);
        long shortlisted = 0, pending = 0, rejected = 0;
        double avgScore = 0;

        List<Application> apps = appRepo.findByJobSeekerId(seekerId, PageRequest.of(0, 100)).getContent();
        int scoredCount = 0; double scoreSum = 0;
        for (Application a : apps) {
            if (a.getStatus() == Application.ApplicationStatus.SHORTLISTED) shortlisted++;
            if (a.getStatus() == Application.ApplicationStatus.REJECTED) rejected++;
            if (a.getStatus() == Application.ApplicationStatus.APPLIED ||
                a.getStatus() == Application.ApplicationStatus.UNDER_REVIEW) pending++;
            Optional<ResumeAnalysis> analysis = analysisRepo.findByApplicationId(a.getId());
            if (analysis.isPresent() && analysis.get().getFinalScore() != null) {
                scoreSum += analysis.get().getFinalScore(); scoredCount++;
            }
        }
        if (scoredCount > 0) avgScore = scoreSum / scoredCount;

        return DashboardStatsResponse.builder()
                .applicationsSent(totalApps).averageAtsScore(Math.round(avgScore * 10.0) / 10.0)
                .shortlistedCount(shortlisted).pendingCount(pending).rejectedCount(rejected).build();
    }
}

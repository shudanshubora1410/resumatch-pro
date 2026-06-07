package com.resumatchpro.dto.response;
import lombok.*;
import java.util.Map;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminAnalyticsResponse {
    private long totalUsers, totalSeekers, totalRecruiters, totalAdmins;
    private long activeJobs, totalApplications, totalResumesAnalyzed;
    private double avgPlatformScore;
    private Map<String, Long> topSkills;
    private Map<String, Long> dailyActivity;
}

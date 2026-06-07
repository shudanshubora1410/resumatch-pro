package com.resumatchpro.dto.response;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardStatsResponse {
    private long applicationsSent; private double averageAtsScore;
    private long shortlistedCount, pendingCount, rejectedCount;
    private long totalActiveJobs, totalApplications;
    private long savedJobsCount; private double resumeQualityScore;
}

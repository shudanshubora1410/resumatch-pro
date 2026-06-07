package com.resumatchpro.service;

import com.resumatchpro.dto.response.DashboardStatsResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service @RequiredArgsConstructor
public class RecruiterService {
    private final JobListingRepository jobListingRepo;
    private final ApplicationRepository appRepo;

    public DashboardStatsResponse getDashboardStats(Long recruiterId) {
        long activeJobs = jobListingRepo.countByRecruiter(recruiterId);
        long totalApps = appRepo.countByRecruiterId(recruiterId);
        return DashboardStatsResponse.builder()
                .totalActiveJobs(activeJobs).totalApplications(totalApps).build();
    }
}

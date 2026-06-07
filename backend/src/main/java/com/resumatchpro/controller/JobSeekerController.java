package com.resumatchpro.controller;

import com.resumatchpro.dto.response.ApiResponse;
import com.resumatchpro.dto.response.PagedResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.service.*;
import com.resumatchpro.repository.UserRepository;
import com.resumatchpro.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/seeker")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;
    private final JobListingService jobListingService;
    private final ApplicationService applicationService;
    private final ResumeService resumeService;
    private final BookmarkService bookmarkService;
    private final RecommendationService recommendationService;
    private final AnalysisService analysisService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse> getDashboardStats(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats",
                jobSeekerService.getDashboardStats(extractUserId(auth))));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse> browseJobs(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(defaultValue = "newest") String sort) {
        Page<JobListing> jobs = jobListingService.getActiveJobs(search, type, location, remote, minExp, sort, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved", jobs));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse> getJobDetail(@PathVariable Long id) {
        jobListingService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success("Job detail", jobListingService.getJob(id)));
    }

    @PostMapping("/jobs/{id}/view")
    public ResponseEntity<ApiResponse> incrementView(@PathVariable Long id) {
        jobListingService.incrementViewCount(id);
        return ResponseEntity.ok(ApiResponse.success("View counted"));
    }

    @PostMapping("/jobs/{id}/bookmark")
    public ResponseEntity<ApiResponse> bookmarkJob(@PathVariable Long id, Authentication auth) {
        bookmarkService.bookmark(extractUserId(auth), id);
        return ResponseEntity.ok(ApiResponse.success("Job bookmarked"));
    }

    @DeleteMapping("/jobs/{id}/bookmark")
    public ResponseEntity<ApiResponse> removeBookmark(@PathVariable Long id, Authentication auth) {
        bookmarkService.removeBookmark(extractUserId(auth), id);
        return ResponseEntity.ok(ApiResponse.success("Bookmark removed"));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse> getBookmarks(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Bookmarks retrieved",
                bookmarkService.getBookmarks(extractUserId(auth), PageRequest.of(page, size))));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse> getRecommendations(
            @RequestParam(required = false) Long resumeId, Authentication auth) {
        Long userId = extractUserId(auth);
        if (resumeId == null) {
            Page<Resume> resumes = resumeService.getUserResumes(userId, PageRequest.of(0, 1));
            if (!resumes.isEmpty()) resumeId = resumes.getContent().get(0).getId();
        }
        return ResponseEntity.ok(ApiResponse.success("Recommendations",
                recommendationService.getRecommendations(resumeId, userId)));
    }

    @PostMapping("/apply/{jobId}")
    public ResponseEntity<ApiResponse> applyToJob(@PathVariable Long jobId,
            @RequestBody Map<String, Long> body, Authentication auth) {
        Long resumeId = body.get("resumeId");
        Application app = applicationService.apply(extractUserId(auth), jobId, resumeId);
        analysisService.triggerAnalysis(app.getId());
        return ResponseEntity.ok(ApiResponse.success("Application submitted", app));
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse> getApplications(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved",
                applicationService.getSeekerApplications(extractUserId(auth), PageRequest.of(page, size))));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApiResponse> getApplicationDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application detail", applicationService.getApplication(id)));
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null)
            throw new UnauthorizedAccessException("Not authenticated");
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
    }
}

package com.resumatchpro.controller;

import com.resumatchpro.dto.request.*;
import com.resumatchpro.dto.response.ApiResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.service.*;
import com.resumatchpro.repository.UserRepository;
import com.resumatchpro.exception.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final JobListingService jobListingService;
    private final ApplicationService applicationService;
    private final AnalysisService analysisService;
    private final InterviewService interviewService;
    private final ExternalScreeningService externalScreeningService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse> getDashboardStats(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats", recruiterService.getDashboardStats(extractUserId(auth))));
    }

    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse> postJob(@Valid @RequestBody JobListingRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Job posted", jobListingService.createJob(extractUserId(auth), request)));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse> getJobs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved", jobListingService.getRecruiterJobs(extractUserId(auth), PageRequest.of(page, size))));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job detail", jobListingService.getJob(id)));
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobListingRequest request, Authentication auth) {
        JobListing job = jobListingService.updateJob(id, extractUserId(auth), request);
        analysisService.triggerRescore(id);
        return ResponseEntity.ok(ApiResponse.success("Job updated — rescoring applicants", job));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse> deleteJob(@PathVariable Long id, Authentication auth) {
        jobListingService.deleteJob(id, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("Job closed"));
    }

    @PutMapping("/jobs/{id}/status")
    public ResponseEntity<ApiResponse> updateJobStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Status updated"));
    }

    @GetMapping("/jobs/{id}/applicants")
    public ResponseEntity<ApiResponse> getApplicants(@PathVariable Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applicants retrieved", applicationService.getJobApplicants(id)));
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody ApplicationStatusUpdateRequest request, Authentication auth) {
        Application app = applicationService.updateStatus(id, extractUserId(auth), request.getStatus(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Status updated to " + app.getStatus(), app));
    }

    @PutMapping("/applications/bulk-status")
    public ResponseEntity<ApiResponse> bulkStatusUpdate(@Valid @RequestBody BulkStatusUpdateRequest request, Authentication auth) {
        applicationService.bulkUpdateStatus(extractUserId(auth), extractUserId(auth), request.getStatus(), request.getApplicationIds());
        return ResponseEntity.ok(ApiResponse.success("Bulk update complete"));
    }

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<ApiResponse> downloadResume(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.success("Resume download endpoint")); }

    @PostMapping("/applications/{id}/notes")
    public ResponseEntity<ApiResponse> addNotes(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        applicationService.addNotes(id, extractUserId(auth), body.get("notes"));
        return ResponseEntity.ok(ApiResponse.success("Notes added"));
    }

    @PostMapping("/applications/{id}/interview")
    public ResponseEntity<ApiResponse> scheduleInterview(@PathVariable Long id, @Valid @RequestBody InterviewScheduleRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Interview scheduled", interviewService.schedule(id, extractUserId(auth), request)));
    }

    @PutMapping("/interview/{id}")
    public ResponseEntity<ApiResponse> updateInterview(@PathVariable Long id, @Valid @RequestBody InterviewScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview updated", interviewService.update(id, request)));
    }

    @DeleteMapping("/interview/{id}")
    public ResponseEntity<ApiResponse> cancelInterview(@PathVariable Long id) { interviewService.cancel(id); return ResponseEntity.ok(ApiResponse.success("Interview cancelled")); }

    @PostMapping("/jobs/{id}/rescore")
    public ResponseEntity<ApiResponse> rescoreJob(@PathVariable Long id) { analysisService.triggerRescore(id); return ResponseEntity.ok(ApiResponse.success("Rescore triggered")); }

    @PostMapping("/jobs/{id}/bulk-screen")
    public ResponseEntity<ApiResponse> bulkScreen(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Bulk screening started"));
    }

    @GetMapping("/jobs/{id}/external-results")
    public ResponseEntity<ApiResponse> getBulkResults(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("External results", externalScreeningService.getResults(id)));
    }

    @GetMapping("/jobs/{id}/analytics")
    public ResponseEntity<ApiResponse> jobAnalytics(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.success("Job analytics")); }

    @GetMapping("/export/applicants/{jobId}")
    public ResponseEntity<String> exportApplicants(@PathVariable Long jobId) { return ResponseEntity.ok("CSV export ready"); }

    @PostMapping("/team/invite")
    public ResponseEntity<ApiResponse> inviteTeamMember(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Invitation sent to " + body.get("email")));
    }

    @GetMapping("/team")
    public ResponseEntity<ApiResponse> getTeam() { return ResponseEntity.ok(ApiResponse.success("Team members", Collections.emptyList())); }

    @DeleteMapping("/team/{memberId}")
    public ResponseEntity<ApiResponse> removeTeamMember(@PathVariable Long memberId) { return ResponseEntity.ok(ApiResponse.success("Member removed")); }

    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new UnauthorizedAccessException("Not authenticated");
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
    }
}

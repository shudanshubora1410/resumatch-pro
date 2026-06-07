package com.resumatchpro.controller;

import com.resumatchpro.dto.response.ApiResponse;
import com.resumatchpro.dto.response.PagedResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ScoringService scoringService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role) {
        Page<User> users = adminService.getUsers(role, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Users retrieved",
                PagedResponse.builder().content(new ArrayList<>(users.getContent())).page(users.getNumber())
                        .size(users.getSize()).totalElements(users.getTotalElements())
                        .totalPages(users.getTotalPages()).last(users.isLast()).first(users.isFirst()).build()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User detail"));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, Boolean> body) {
        User user = adminService.updateUserStatus(id, body.getOrDefault("active", true));
        return ResponseEntity.ok(ApiResponse.success("User status updated", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        adminService.updateUserStatus(id, false);
        return ResponseEntity.ok(ApiResponse.success("User deactivated"));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("All jobs"));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse> removeJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job removed"));
    }

    @PostMapping("/jobs/{id}/rescore")
    public ResponseEntity<ApiResponse> rescoreJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Admin rescore triggered"));
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<ApiResponse> getAnalytics() {
        Map<String, Object> stats = adminService.getPlatformOverview();
        return ResponseEntity.ok(ApiResponse.success("Platform analytics", stats));
    }

    @GetMapping("/analytics/trends")
    public ResponseEntity<ApiResponse> getTrends() {
        return ResponseEntity.ok(ApiResponse.success("Trends data"));
    }

    @GetMapping("/analytics/skills")
    public ResponseEntity<ApiResponse> getTopSkills() {
        return ResponseEntity.ok(ApiResponse.success("Top skills"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminAuditLog> logs = adminService.getAuditLogs(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Audit logs", logs));
    }

    @GetMapping("/export/users")
    public ResponseEntity<String> exportUsers() {
        String csv = adminService.exportUsersCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .body(csv);
    }

    @GetMapping("/export/jobs")
    public ResponseEntity<String> exportJobs() {
        return ResponseEntity.ok("CSV data");
    }

    @GetMapping("/export/applications")
    public ResponseEntity<String> exportApplications() {
        return ResponseEntity.ok("CSV data");
    }

    @GetMapping("/system/stats")
    public ResponseEntity<ApiResponse> getSystemStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("memory", Runtime.getRuntime().totalMemory());
        stats.put("platformAvgScore", scoringService.getPlatformAverage());
        return ResponseEntity.ok(ApiResponse.success("System stats", stats));
    }
}

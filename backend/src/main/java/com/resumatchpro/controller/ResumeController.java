package com.resumatchpro.controller;

import com.resumatchpro.dto.response.ApiResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.service.*;
import com.resumatchpro.repository.UserRepository;
import com.resumatchpro.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/seeker/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadResume(@RequestParam("file") MultipartFile file, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded", resumeService.uploadResume(extractUserId(auth), file)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllResumes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Resumes retrieved", resumeService.getUserResumes(extractUserId(auth), PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getResume(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Resume detail", resumeService.getResume(id, extractUserId(auth))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteResume(@PathVariable Long id, Authentication auth) {
        resumeService.deleteResume(id, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("Resume deleted"));
    }

    @GetMapping("/{id}/quality-check")
    public ResponseEntity<ApiResponse> qualityCheck(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Quality check complete", resumeService.qualityCheck(id, extractUserId(auth))));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse> compareResumes(@RequestParam Long r1, @RequestParam Long r2, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Comparison complete", resumeService.compareResumes(r1, r2, extractUserId(auth))));
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new UnauthorizedAccessException("Not authenticated");
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
    }
}

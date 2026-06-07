package com.resumatchpro.service;

import com.resumatchpro.dto.request.JobListingRequest;
import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.utility.InputSanitizerUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JobListingService {

    private static final Logger log = LoggerFactory.getLogger(JobListingService.class);

    private final JobListingRepository jobListingRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final InputSanitizerUtil sanitizer;

    // ==================== CRUD ====================

    @Transactional
    @CacheEvict(value = "activeJobs", allEntries = true)
    public JobListing createJob(Long recruiterId, JobListingRequest request) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", recruiterId));

        JobListing job = JobListing.builder()
                .recruiter(recruiter)
                .jobTitle(sanitizer.sanitizePlainText(request.getJobTitle()))
                .jobDescription(sanitizer.sanitize(request.getJobDescription()))
                .requiredSkills(request.getRequiredSkills())
                .preferredSkills(request.getPreferredSkills())
                .atsKeywords(request.getAtsKeywords())
                .minExperienceYears(request.getMinExperienceYears())
                .educationRequirement(request.getEducationRequirement())
                .jobType(parseJobType(request.getJobType()))
                .industry(request.getIndustry())
                .location(request.getLocation())
                .isRemote(request.getIsRemote() != null ? request.getIsRemote() : false)
                .salaryRange(request.getSalaryRange())
                .numberOfOpenings(request.getNumberOfOpenings() != null ? request.getNumberOfOpenings() : 1)
                .applicationDeadline(request.getApplicationDeadline())
                .status("DRAFT".equals(request.getStatus()) ? JobListing.JobStatus.DRAFT : JobListing.JobStatus.ACTIVE)
                .build();

        job = jobListingRepository.save(job);
        log.info("Job listing created: id={} title='{}' by recruiter={}", job.getId(), job.getJobTitle(), recruiterId);
        return job;
    }

    @Transactional
    @CacheEvict(value = {"activeJobs", "jobListings", "analysisReports", "skillGaps"}, allEntries = true)
    public JobListing updateJob(Long jobId, Long recruiterId, JobListingRequest request) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedAccessException("You can only update your own job listings");
        }

        // Track if requirements changed (triggers rescore)
        boolean requirementsChanged = hasRequirementsChanged(job, request);

        job.setJobTitle(sanitizer.sanitizePlainText(request.getJobTitle()));
        job.setJobDescription(sanitizer.sanitize(request.getJobDescription()));
        job.setRequiredSkills(request.getRequiredSkills());
        job.setPreferredSkills(request.getPreferredSkills());
        job.setAtsKeywords(request.getAtsKeywords());
        job.setMinExperienceYears(request.getMinExperienceYears());
        job.setEducationRequirement(request.getEducationRequirement());
        job.setJobType(parseJobType(request.getJobType()));
        job.setIndustry(request.getIndustry());
        job.setLocation(request.getLocation());
        job.setIsRemote(request.getIsRemote() != null ? request.getIsRemote() : false);
        job.setSalaryRange(request.getSalaryRange());
        job.setNumberOfOpenings(request.getNumberOfOpenings());
        job.setApplicationDeadline(request.getApplicationDeadline());

        job = jobListingRepository.save(job);

        log.info("Job listing updated: id={} requirementsChanged={}", jobId, requirementsChanged);

        // If requirements changed, mark for rescore (handled by caller or AnalysisService)
        if (requirementsChanged) {
            job.setUpdatedAt(java.time.LocalDateTime.now());
            log.info("Job {} requirements updated — rescore needed for all applicants", jobId);
        }

        return job;
    }

    @Transactional
    @CacheEvict(value = {"activeJobs", "jobListings"}, allEntries = true)
    public void deleteJob(Long jobId, Long recruiterId) {
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new UnauthorizedAccessException("You can only delete your own job listings");
        }

        job.setStatus(JobListing.JobStatus.CLOSED);
        job.softDelete();
        jobListingRepository.save(job);
        log.info("Job listing closed: id={}", jobId);
    }

    // ==================== QUERIES ====================

    @Cacheable(value = "activeJobs", unless = "#result.content.isEmpty()")
    public Page<JobListing> getActiveJobs(String search, String jobType, String location,
                                           Boolean remote, Integer minExp, String sort, Pageable pageable) {
        Specification<JobListing> spec = Specification.where(null);

        // Active and not deleted
        spec = spec.and((root, query, cb) ->
                cb.and(
                    cb.equal(root.get("status"), JobListing.JobStatus.ACTIVE),
                    cb.isNull(root.get("deletedAt")),
                    cb.or(
                        cb.isNull(root.get("applicationDeadline")),
                        cb.greaterThanOrEqualTo(root.get("applicationDeadline"), LocalDate.now())
                    )
                ));

        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("jobTitle")), like),
                    cb.like(cb.lower(root.get("jobDescription")), like),
                    cb.like(cb.lower(root.get("requiredSkills")), like)
                ));
        }

        if (jobType != null) {
            try {
                JobListing.JobType type = JobListing.JobType.valueOf(jobType.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("jobType"), type));
            } catch (IllegalArgumentException ignored) {}
        }

        if (location != null && !location.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"),
                    cb.isTrue(root.get("isRemote"))
                ));
        }

        return jobListingRepository.findAll(spec, pageable);
    }

    public Page<JobListing> getRecruiterJobs(Long recruiterId, Pageable pageable) {
        return jobListingRepository.findByRecruiterIdAndDeletedAtIsNull(recruiterId, pageable);
    }

    public JobListing getJob(Long jobId) {
        return jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
    }

    public List<JobListing> getAllActiveJobs() {
        return jobListingRepository.findAllActiveJobs();
    }

    // ==================== HELPERS ====================

    private boolean hasRequirementsChanged(JobListing existing, JobListingRequest request) {
        return !Objects.equals(existing.getRequiredSkills(), request.getRequiredSkills())
            || !Objects.equals(existing.getPreferredSkills(), request.getPreferredSkills())
            || !Objects.equals(existing.getAtsKeywords(), request.getAtsKeywords())
            || !Objects.equals(existing.getMinExperienceYears(), request.getMinExperienceYears())
            || !Objects.equals(existing.getEducationRequirement(), request.getEducationRequirement());
    }

    private JobListing.JobType parseJobType(String type) {
        if (type == null) return JobListing.JobType.FULL_TIME;
        try {
            return JobListing.JobType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JobListing.JobType.FULL_TIME;
        }
    }

    @Transactional
    public void incrementViewCount(Long jobId) {
        jobListingRepository.findById(jobId).ifPresent(job -> {
            job.setViewCount(job.getViewCount() + 1);
            jobListingRepository.save(job);
        });
    }
}

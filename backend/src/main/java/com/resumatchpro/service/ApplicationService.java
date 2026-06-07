package com.resumatchpro.service;

import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final JobListingRepository jobListingRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final RecruiterProfileRepository recruiterProfileRepository;

    @Transactional
    public Application apply(Long seekerId, Long jobId, Long resumeId) {
        if (applicationRepository.existsByJobSeekerIdAndJobListingId(seekerId, jobId))
            throw new DuplicateApplicationException("You have already applied to this job");
        User seeker = userRepository.findById(seekerId).orElseThrow(() -> new ResourceNotFoundException("User", seekerId));
        JobListing job = jobListingRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (job.getStatus() != JobListing.JobStatus.ACTIVE) throw new RuntimeException("This job is no longer accepting applications");
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));
        if (!resume.getUser().getId().equals(seekerId)) throw new UnauthorizedAccessException("You can only use your own resumes");

        Application application = Application.builder().jobSeeker(seeker).jobListing(job).resume(resume).status(Application.ApplicationStatus.APPLIED).build();
        application = applicationRepository.save(application);

        String company = getCompanyName(job);
        notificationService.create(job.getRecruiter(), "New Application", seeker.getFullName() + " applied for " + job.getJobTitle(), Notification.NotificationType.APPLICATION_RECEIVED, "/recruiter/applicants.html?jobId=" + jobId);
        notificationService.create(seeker, "Application Submitted", "Your application for " + job.getJobTitle() + " has been submitted", Notification.NotificationType.SYSTEM, "/seeker/my-applications.html");
        try { emailService.sendApplicationReceivedEmail(seeker.getEmail(), seeker.getFullName(), job.getJobTitle(), company); } catch (Exception e) { log.warn("Failed to send application email: {}", e.getMessage()); }

        log.info("Application submitted: seeker={} job={} resume={}", seekerId, jobId, resumeId);
        return application;
    }

    public Page<Application> getSeekerApplications(Long seekerId, Pageable pageable) { return applicationRepository.findByJobSeekerId(seekerId, pageable); }
    public Application getApplication(Long applicationId) { return applicationRepository.findById(applicationId).orElseThrow(() -> new ResourceNotFoundException("Application", applicationId)); }
    public List<Application> getJobApplicants(Long jobId) { return applicationRepository.findAllByJobListingId(jobId); }

    @Transactional
    public Application updateStatus(Long applicationId, Long recruiterId, String newStatus, String notes) {
        Application application = getApplication(applicationId);
        if (!application.getJobListing().getRecruiter().getId().equals(recruiterId)) throw new UnauthorizedAccessException("You can only manage applications for your own jobs");
        Application.ApplicationStatus status;
        try { status = Application.ApplicationStatus.valueOf(newStatus.toUpperCase()); } catch (IllegalArgumentException e) { throw new RuntimeException("Invalid status: " + newStatus); }
        Application.ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(status);
        if (notes != null && !notes.isBlank()) application.setRecruiterNotes(notes);
        application = applicationRepository.save(application);

        notificationService.create(application.getJobSeeker(), "Application " + status.name().replace("_", " "), "Your application for " + application.getJobListing().getJobTitle() + " has been " + status.name().toLowerCase().replace("_", " "), mapStatusToNotificationType(status), "/seeker/my-applications.html");
        try { emailService.sendStatusChangeEmail(application.getJobSeeker().getEmail(), application.getJobSeeker().getFullName(), application.getJobListing().getJobTitle(), getCompanyName(application.getJobListing()), status.name()); } catch (Exception e) { log.warn("Failed to send status email: {}", e.getMessage()); }

        log.info("Application status changed: id={} {} -> {}", applicationId, oldStatus, status);
        return application;
    }

    @Transactional
    public void bulkUpdateStatus(Long jobId, Long recruiterId, String newStatus, List<Long> applicationIds) { for (Long appId : applicationIds) updateStatus(appId, recruiterId, newStatus, null); }

    @Transactional
    public void addNotes(Long applicationId, Long recruiterId, String notes) {
        Application app = getApplication(applicationId);
        if (!app.getJobListing().getRecruiter().getId().equals(recruiterId)) throw new UnauthorizedAccessException("Not authorized");
        app.setRecruiterNotes(notes);
        applicationRepository.save(app);
    }

    public Map<String, Object> getRecruiterStats(Long recruiterId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeJobs", jobListingRepository.countByRecruiter(recruiterId));
        stats.put("totalApplications", applicationRepository.countByRecruiterId(recruiterId));
        stats.put("shortlisted", 0L); stats.put("rejected", 0L); stats.put("pending", 0L);
        return stats;
    }

    private Notification.NotificationType mapStatusToNotificationType(Application.ApplicationStatus status) {
        return switch (status) { case SHORTLISTED -> Notification.NotificationType.SHORTLISTED; case REJECTED -> Notification.NotificationType.REJECTED; case INTERVIEW_SCHEDULED -> Notification.NotificationType.INTERVIEW_SCHEDULED; default -> Notification.NotificationType.STATUS_CHANGE; };
    }

    private String getCompanyName(JobListing job) {
        return recruiterProfileRepository.findByUserId(job.getRecruiter().getId()).map(RecruiterProfile::getCompanyName).orElse("Unknown Company");
    }
}

package com.resumatchpro.scheduler;

import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeadlineReminderScheduler.class);
    private final JobListingRepository jobListingRepository;
    private final JobBookmarkRepository bookmarkRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.scheduler.deadline-reminder-cron}")
    public void checkDeadlines() {
        log.info("Running deadline reminder check...");
        try {
            List<JobListing> activeJobs = jobListingRepository.findAllActiveJobs();
            LocalDate today = LocalDate.now();
            int remindersSent = 0;

            for (JobListing job : activeJobs) {
                if (job.getApplicationDeadline() == null) continue;
                boolean isThreeDay = job.getApplicationDeadline().isEqual(today.plusDays(3));
                boolean isOneDay = job.getApplicationDeadline().isEqual(today.plusDays(1));
                if (!isThreeDay && !isOneDay) continue;

                String urgency = isOneDay ? "URGENT: " : "";
                String title = urgency + "Deadline approaching — " + job.getJobTitle();
                String msg = isOneDay
                    ? "Deadline for " + job.getJobTitle() + " is TOMORROW. Apply now!"
                    : "Deadline for " + job.getJobTitle() + " is in 3 days.";

                for (JobBookmark bm : bookmarkRepository.findByJobListingId(job.getId())) {
                    notificationService.create(bm.getUser(), title, msg,
                        Notification.NotificationType.DEADLINE_REMINDER,
                        "/seeker/job-detail.html?id=" + job.getId());
                    remindersSent++;
                }
            }
            log.info("Deadline check done: {} jobs, {} reminders sent.", activeJobs.size(), remindersSent);
        } catch (Exception e) {
            log.error("Deadline reminder error: {}", e.getMessage(), e);
        }
    }
}

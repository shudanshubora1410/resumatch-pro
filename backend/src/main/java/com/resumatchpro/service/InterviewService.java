package com.resumatchpro.service;

import com.resumatchpro.dto.request.InterviewScheduleRequest;
import com.resumatchpro.exception.ResourceNotFoundException;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewScheduleRepository interviewRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public InterviewSchedule schedule(Long applicationId, Long recruiterId,
                                       InterviewScheduleRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));

        InterviewSchedule interview = InterviewSchedule.builder()
                .application(application)
                .scheduledBy(User.builder().id(recruiterId).build())
                .interviewDate(request.getInterviewDate())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                .interviewMode(request.getInterviewMode())
                .locationOrLink(request.getLocationOrLink())
                .notes(request.getNotes())
                .status(InterviewSchedule.InterviewStatus.SCHEDULED)
                .build();

        interview = interviewRepository.save(interview);

        application.setStatus(Application.ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        return interview;
    }

    @Transactional
    public InterviewSchedule update(Long interviewId, InterviewScheduleRequest request) {
        InterviewSchedule interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", interviewId));
        if (request.getInterviewDate() != null) interview.setInterviewDate(request.getInterviewDate());
        if (request.getDurationMinutes() != null) interview.setDurationMinutes(request.getDurationMinutes());
        if (request.getInterviewMode() != null) interview.setInterviewMode(request.getInterviewMode());
        if (request.getLocationOrLink() != null) interview.setLocationOrLink(request.getLocationOrLink());
        if (request.getNotes() != null) interview.setNotes(request.getNotes());
        return interviewRepository.save(interview);
    }

    @Transactional
    public void cancel(Long interviewId) {
        InterviewSchedule interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", interviewId));
        interview.setStatus(InterviewSchedule.InterviewStatus.CANCELLED);
        interviewRepository.save(interview);
    }
}

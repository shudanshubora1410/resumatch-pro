package com.resumatchpro.dto.request;

import com.resumatchpro.model.InterviewSchedule;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InterviewScheduleRequest {

    @NotNull(message = "Interview date is required")
    private LocalDateTime interviewDate;

    private Integer durationMinutes = 60;

    @NotNull(message = "Interview mode is required")
    private InterviewSchedule.InterviewMode interviewMode = InterviewSchedule.InterviewMode.VIDEO;

    private String locationOrLink;
    private String notes;
}

package com.resumatchpro.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class JobListingRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String jobDescription;
    private String requiredSkills;
    private String preferredSkills;
    private String atsKeywords;

    private Integer minExperienceYears = 0;

    private String educationRequirement;
    private String jobType = "FULL_TIME";
    private String industry;
    private String location;
    private Boolean isRemote = false;
    private String salaryRange;
    private Integer numberOfOpenings = 1;
    private LocalDate applicationDeadline;
    private String status = "ACTIVE";
}

package com.resumatchpro.dto.response;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobListingResponse {
    private Long id; private String jobTitle, jobDescription;
    private String requiredSkills, preferredSkills, atsKeywords;
    private Integer minExperienceYears; private String educationRequirement;
    private String jobType, industry, location; private Boolean isRemote;
    private String salaryRange; private Integer numberOfOpenings;
    private LocalDate applicationDeadline; private String status;
    private Integer viewCount; private String companyName;
    private LocalDate createdAt; private Integer applicantCount;
    private Double avgScore; private Double matchPercentage;
}

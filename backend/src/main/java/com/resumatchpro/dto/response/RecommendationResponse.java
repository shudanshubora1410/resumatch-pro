package com.resumatchpro.dto.response;
import lombok.*;
import java.util.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RecommendationResponse {
    private Long jobId; private String jobTitle, companyName, location, jobType;
    private double matchPercentage; private List<String> matchedSkills, missingSkills;
}

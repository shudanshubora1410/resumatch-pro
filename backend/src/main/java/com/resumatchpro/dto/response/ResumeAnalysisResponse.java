package com.resumatchpro.dto.response;
import lombok.*;
import java.util.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ResumeAnalysisResponse {
    private Long applicationId; private int analysisVersion; private String analysisStatus;
    private String candidateName, candidateEmail, jobTitle, companyName;
    private Long jobId; private String analysisDate; private long processingTimeMs;
    private int finalScore, maxPossibleScore; private String grade, gradeLabel, atsStatus, atsStatusColor;
    private Map<String, ScoreCategory> scoreBreakdown;
    private SkillAnalysis skillAnalysis; private KeywordAnalysis keywordAnalysis;
    private ExperienceAnalysis experienceAnalysis; private AchievementAnalysis achievementAnalysis;
    private StructureAnalysis structureAnalysis; private EducationAnalysis educationAnalysis;
    private List<String> weakAreas; private List<ImprovementSuggestion> improvementSuggestions;
    private String overallFeedback;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ScoreCategory { public int score, maxScore, percentage; public String label, status; }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SkillAnalysis {
        public List<String> extractedSkills, matchedRequiredSkills, missingRequiredSkills,
                          matchedPreferredSkills, missingPreferredSkills;
        public int skillMatchPercentage, requiredSkillMatchPercentage, preferredSkillMatchPercentage, totalUniqueSkillsInResume;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class KeywordAnalysis {
        public List<String> recruiterDefinedKeywords, matchedKeywords, missingKeywords;
        public int keywordMatchPercentage; public Map<String, Double> keywordConfidenceScores;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExperienceAnalysis {
        public List<String> detectedActionVerbs, weakPhrasesFound;
        public int estimatedYearsOfExperience, requiredYearsOfExperience, experienceGapYears;
        public double technicalDepthScore;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AchievementAnalysis {
        public List<String> measurableAchievements;
        public int percentagePatternCount, numericalPatternCount, growthStatementCount;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StructureAnalysis {
        public List<String> detectedSections, missingSections;
        public Map<String, Boolean> contactInfoPresent; public int resumeWordCount; public boolean atsFriendly;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EducationAnalysis {
        public String detectedEducation, requiredEducation, matchType; public int educationScore;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ImprovementSuggestion {
        public int priority; public String area, suggestion, impact; public int estimatedScoreGain;
    }
}

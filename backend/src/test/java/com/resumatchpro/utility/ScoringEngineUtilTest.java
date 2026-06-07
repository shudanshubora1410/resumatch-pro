package com.resumatchpro.utility;

import com.resumatchpro.model.JobListing;
import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ScoringEngineUtilTest {

    private ScoringEngineUtil engine;
    private NLPProcessorUtil nlpProcessor;

    @BeforeEach
    void setUp() {
        StopwordsUtil sw = new StopwordsUtil();
        SkillSynonymUtil syn = new SkillSynonymUtil();
        ActionVerbUtil av = new ActionVerbUtil();
        SectionDetectorUtil sd = new SectionDetectorUtil();
        KeywordExtractorUtil ke = new KeywordExtractorUtil();
        AchievementDetectorUtil ad = new AchievementDetectorUtil();
        GradeCalculatorUtil gc = new GradeCalculatorUtil();

        nlpProcessor = new NLPProcessorUtil(sw, syn, av, sd, ke, ad);
        engine = new ScoringEngineUtil(ke, syn, sd, av, ad, gc);
    }

    @Test
    void testPerfectMatch_shouldScoreHigh() {
        String resume = "SKILLS: Java, Spring Boot, MySQL, Docker, Kubernetes, AWS\n" +
                "EXPERIENCE: 5 years developing Java Spring Boot microservices\n" +
                "Architected cloud-native platform serving 1M+ requests daily\n" +
                "EDUCATION: B.Tech Computer Science\n" +
                "CERTIFICATIONS: AWS Solutions Architect";
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume);

        JobListing job = JobListing.builder()
                .requiredSkills("Java,Spring Boot,MySQL,Docker")
                .preferredSkills("Kubernetes,AWS")
                .atsKeywords("Java,Spring Boot,Microservices,Docker,Kubernetes,AWS")
                .minExperienceYears(3)
                .educationRequirement("B.Tech Computer Science")
                .build();

        ScoringEngineUtil.ScoreResult result = engine.score(processed, job);
        assertTrue(result.finalScore >= 80, "Strong match should score >= 80, got: " + result.finalScore);
    }

    @Test
    void testWeakResume_shouldScoreLow() {
        String resume = "SKILLS: Microsoft Office, Basic HTML\nEXPERIENCE: Intern for 6 months\nEDUCATION: BCA";
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume);

        JobListing job = JobListing.builder()
                .requiredSkills("Java,Spring Boot,MySQL,Docker,Kubernetes")
                .atsKeywords("Java,Spring Boot,Microservices,JPA,Docker,AWS")
                .minExperienceYears(3)
                .educationRequirement("B.Tech Computer Science")
                .build();

        ScoringEngineUtil.ScoreResult result = engine.score(processed, job);
        assertTrue(result.finalScore < 50, "Weak match should score < 50, got: " + result.finalScore);
    }

    @Test
    void testEmptyResume_shouldHandleGracefully() {
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process("");
        JobListing job = JobListing.builder().requiredSkills("Java,Spring").atsKeywords("Java").build();
        ScoringEngineUtil.ScoreResult result = engine.score(processed, job);
        assertNotNull(result);
        assertTrue(result.finalScore >= 0 && result.finalScore <= 100);
    }

    @Test
    void testSixCategories_shouldAllHaveScores() {
        String resume = "SKILLS: Java, Git\nEXPERIENCE: 2 years at TCS\nDeveloped REST APIs\nEDUCATION: B.Tech IT";
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume);

        JobListing job = JobListing.builder()
                .requiredSkills("Java")
                .atsKeywords("Java,REST API,Git")
                .minExperienceYears(2)
                .educationRequirement("B.Tech Computer Science")
                .build();

        ScoringEngineUtil.ScoreResult result = engine.score(processed, job);
        assertNotNull(result.keywordMatch);
        assertNotNull(result.skillRelevance);
        assertNotNull(result.experienceQuality);
        assertNotNull(result.achievements);
        assertNotNull(result.formatting);
        assertNotNull(result.education);
        assertTrue(result.keywordMatch.score >= 0);
        assertTrue(result.skillRelevance.score >= 0);
        assertTrue(result.experienceQuality.score >= 0);
        assertTrue(result.achievements.score >= 0);
        assertTrue(result.formatting.score >= 0);
        assertTrue(result.education.score >= 0);
    }

    @Test
    void testGradeCalculation_shouldBeCorrect() {
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process("");
        JobListing job = JobListing.builder().requiredSkills("A").atsKeywords("A").build();
        ScoringEngineUtil.ScoreResult result = engine.score(processed, job);
        assertNotNull(result.grade);
        assertNotNull(result.grade.grade);
        assertNotNull(result.grade.label);
    }

    @Test
    void testSuggestions_shouldBeGenerated() {
        String resume = "SKILLS: basic stuff\nEXPERIENCE: worked on projects";
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume);

        JobListing job = JobListing.builder()
                .requiredSkills("Java,Docker,Kubernetes")
                .atsKeywords("Java,Spring Boot,Docker")
                .minExperienceYears(3)
                .educationRequirement("B.Tech CS")
                .build();

        ScoringEngineUtil.ScoreResult result = engine.score(processed, job);
        assertNotNull(result.suggestions);
        assertFalse(result.suggestions.isEmpty());
        assertNotNull(result.weakAreas);
        assertNotNull(result.overallFeedback);
    }
}

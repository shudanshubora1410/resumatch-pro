package com.resumatchpro.service;

import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.utility.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private ResumeAnalysisRepository analysisRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private NotificationService notificationService;

    private NLPProcessorUtil nlpProcessor;
    private ScoringEngineUtil scoringEngine;
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        // Build real NLP and Scoring utilities with mocked dependencies
        StopwordsUtil stopwordsUtil = new StopwordsUtil();
        SkillSynonymUtil synonymUtil = new SkillSynonymUtil();
        ActionVerbUtil actionVerbUtil = new ActionVerbUtil();
        SectionDetectorUtil sectionDetectorUtil = new SectionDetectorUtil();
        KeywordExtractorUtil keywordExtractorUtil = new KeywordExtractorUtil();
        AchievementDetectorUtil achievementDetectorUtil = new AchievementDetectorUtil();

        nlpProcessor = new NLPProcessorUtil(stopwordsUtil, synonymUtil, actionVerbUtil,
                sectionDetectorUtil, keywordExtractorUtil, achievementDetectorUtil);

        GradeCalculatorUtil gradeCalculator = new GradeCalculatorUtil();
        scoringEngine = new ScoringEngineUtil(keywordExtractorUtil, synonymUtil, sectionDetectorUtil, actionVerbUtil,
                achievementDetectorUtil, gradeCalculator);

        scoringService = new ScoringService(resumeRepository, analysisRepository,
                null, applicationRepository, nlpProcessor, scoringEngine, notificationService);
    }

    @Test
    void testNLPProcessing_shouldExtractSkills() {
        String resumeText = "Skills: Java, Python, MySQL\nExperience: 3 years developing Spring Boot REST APIs\nEducation: B.Tech Computer Science";

        NLPProcessorUtil.ProcessedResume result = nlpProcessor.process(resumeText);

        assertNotNull(result);
        assertTrue(result.getWordCount() > 0);
        assertFalse(result.getExtractedSkills().isEmpty());
        assertTrue(result.getExtractedSkills().contains("java"));
        assertTrue(result.getDetectedSections().contains("SKILLS"));
        assertTrue(result.getDetectedSections().contains("EXPERIENCE"));
        assertEquals(3, result.getEstimatedExperienceYears());
    }

    @Test
    void testNLPProcessing_shouldDetectSections() {
        String resumeText = "SUMMARY\nSoftware engineer with 5 years experience\n\nSKILLS\nJava, Spring Boot, Docker\n\nEXPERIENCE\nDeveloped microservices at TCS\n\nEDUCATION\nB.Tech IT";

        NLPProcessorUtil.ProcessedResume result = nlpProcessor.process(resumeText);

        assertTrue(result.getDetectedSections().contains("SUMMARY"));
        assertTrue(result.getDetectedSections().contains("SKILLS"));
        assertTrue(result.getDetectedSections().contains("EXPERIENCE"));
        assertTrue(result.getDetectedSections().contains("EDUCATION"));
        assertFalse(result.getDetectedSections().contains("PROJECTS"));
    }

    @Test
    void testScoring_shouldProduceValidRange() {
        String resumeText = "Skills: Java, Spring Boot, MySQL, REST API, Git, Maven\n" +
                "Experience: 3 years developing Spring Boot microservices at Infosys\n" +
                "Improved API response time by 40%. Served 10,000+ daily users.\n" +
                "Education: B.Tech Computer Science";

        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resumeText);

        JobListing job = JobListing.builder()
                .requiredSkills("Java,Spring Boot,MySQL,Docker,Kafka")
                .preferredSkills("Git,Maven,Kubernetes,AWS")
                .atsKeywords("Java,Spring Boot,Microservices,JPA,Docker,Kubernetes,AWS,CI/CD")
                .minExperienceYears(3)
                .educationRequirement("B.Tech Computer Science")
                .build();

        ScoringEngineUtil.ScoreResult result = scoringEngine.score(processed, job);

        assertNotNull(result);
        assertTrue(result.finalScore >= 0 && result.finalScore <= 100,
                "Score should be 0-100, got: " + result.finalScore);
        assertNotNull(result.grade);
        assertNotNull(result.grade.grade);
        assertNotNull(result.suggestions);
        assertFalse(result.weakAreas.isEmpty());
    }

    @Test
    void testEmptyResume_shouldHandleGracefully() {
        NLPProcessorUtil.ProcessedResume result = nlpProcessor.process("");
        assertNotNull(result);
        assertEquals(0, result.getWordCount());
        assertTrue(result.getExtractedSkills().isEmpty());
    }

    @Test
    void testActionVerbDetection() {
        String experience = "Architected cloud-native microservices platform\n" +
                "Deployed Kubernetes clusters across 3 regions\n" +
                "Spearheaded migration from monolith to microservices\n" +
                "worked on backend API development\n" +
                "helped with code reviews";

        NLPProcessorUtil.ProcessedResume result = nlpProcessor.process(
                "EXPERIENCE\n" + experience);

        List<String> verbs = result.getDetectedActionVerbs();
        assertTrue(verbs.contains("architected"));
        assertTrue(verbs.contains("deployed"));
        assertTrue(verbs.contains("spearheaded"));
        // Weak phrases should be caught
        assertTrue(result.getWeakPhrasesFound().contains("worked on"));
    }

    @Test
    void testAchievementDetection() {
        String achievements = "improved performance by 40%\n" +
                "reduced load time by 30%\n" +
                "served 10,000+ users globally\n" +
                "generated $2M in revenue\n";

        NLPProcessorUtil.ProcessedResume result = nlpProcessor.process(achievements);

        assertFalse(result.getAchievements().isEmpty());
        assertTrue(result.getPercentageAchievementCount() >= 2);
    }
}

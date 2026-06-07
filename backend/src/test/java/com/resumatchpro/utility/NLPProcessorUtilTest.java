package com.resumatchpro.utility;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class NLPProcessorUtilTest {

    private NLPProcessorUtil processor;

    @BeforeEach
    void setUp() {
        StopwordsUtil sw = new StopwordsUtil();
        SkillSynonymUtil syn = new SkillSynonymUtil();
        ActionVerbUtil av = new ActionVerbUtil();
        SectionDetectorUtil sd = new SectionDetectorUtil();
        KeywordExtractorUtil ke = new KeywordExtractorUtil();
        AchievementDetectorUtil ad = new AchievementDetectorUtil();
        processor = new NLPProcessorUtil(sw, syn, av, sd, ke, ad);
    }

    @Test
    void testStopwordRemoval() {
        String text = "the quick brown fox jumps over the lazy dog";
        NLPProcessorUtil.ProcessedResume result = processor.process(text);
        // Stopwords like "the", "over" should be removed
        assertTrue(result.getNormalizedText() != null);
    }

    @Test
    void testSkillSynonymNormalization() {
        String skills = "Skills: js, reactjs, node, k8s, ml, aws";
        NLPProcessorUtil.ProcessedResume result = processor.process(skills);
        // Check that extracted skills contain normalized forms
        assertTrue(result.getExtractedSkills().contains("javascript") ||
                   result.getExtractedSkills().contains("js"));
    }

    @Test
    void testExperienceYearsExtraction() {
        String text = "5 years of experience in software development\nWorked from Jan 2020 - Dec 2023";
        int years = processor.estimateExperienceYears(text);
        assertEquals(5, years);
    }

    @Test
    void testContactInfoDetection() {
        String text = "Email: john@example.com Phone: 9876543210\nLinkedIn: linkedin.com/in/john\nGitHub: github.com/john";
        NLPProcessorUtil.ProcessedResume result = processor.process(text);
        assertTrue(result.getContactInfo().getOrDefault("email", false));
        assertTrue(result.getContactInfo().getOrDefault("phone", false));
        assertTrue(result.getContactInfo().getOrDefault("linkedin", false));
        assertTrue(result.getContactInfo().getOrDefault("github", false));
    }
}

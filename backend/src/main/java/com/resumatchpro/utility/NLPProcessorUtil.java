package com.resumatchpro.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Component
public class NLPProcessorUtil {

    private static final Logger log = LoggerFactory.getLogger(NLPProcessorUtil.class);

    private final StopwordsUtil stopwordsUtil;
    private final SkillSynonymUtil skillSynonymUtil;
    private final ActionVerbUtil actionVerbUtil;
    private final SectionDetectorUtil sectionDetectorUtil;
    private final KeywordExtractorUtil keywordExtractorUtil;
    private final AchievementDetectorUtil achievementDetectorUtil;

    public NLPProcessorUtil(StopwordsUtil stopwordsUtil,
                            SkillSynonymUtil skillSynonymUtil,
                            ActionVerbUtil actionVerbUtil,
                            SectionDetectorUtil sectionDetectorUtil,
                            KeywordExtractorUtil keywordExtractorUtil,
                            AchievementDetectorUtil achievementDetectorUtil) {
        this.stopwordsUtil = stopwordsUtil;
        this.skillSynonymUtil = skillSynonymUtil;
        this.actionVerbUtil = actionVerbUtil;
        this.sectionDetectorUtil = sectionDetectorUtil;
        this.keywordExtractorUtil = keywordExtractorUtil;
        this.achievementDetectorUtil = achievementDetectorUtil;
    }

    /**
     * Process raw resume text through the complete NLP pipeline.
     * Returns a rich analysis object with all extracted information.
     */
    public ProcessedResume process(String rawText) {
        long startTime = System.currentTimeMillis();

        if (rawText == null || rawText.isBlank()) {
            return ProcessedResume.empty();
        }

        // Step 1: Normalize text
        String normalized = normalize(rawText);

        // Step 2: Detect sections
        Map<String, String> sections = sectionDetectorUtil.detectSections(normalized);
        Set<String> detectedSections = sectionDetectorUtil.getDetectedSectionNames(sections);
        List<String> missingSections = sectionDetectorUtil.getMissingSections(sections);

        // Step 3: Tokenize and remove stopwords
        List<String> tokens = tokenize(normalized);
        List<String> cleanTokens = stopwordsUtil.removeStopwords(tokens);

        // Step 4: Normalize skill synonyms
        List<String> normalizedTokens = skillSynonymUtil.normalizeAll(cleanTokens);

        // Step 5: Extract skills
        Set<String> extractedSkills = keywordExtractorUtil.extractSkills(normalized);

        // Step 6: Detect action verbs in experience section
        String experienceContent = sections.getOrDefault("EXPERIENCE", "");
        List<String> expBullets = sectionDetectorUtil.extractBulletPoints(experienceContent);
        List<String> detectedVerbs = detectActionVerbs(expBullets);
        List<String> weakPhrases = actionVerbUtil.findWeakPhrases(experienceContent);

        // Step 7: Detect experience years
        int estimatedYears = estimateExperienceYears(normalized);

        // Step 8: Extract achievements
        List<String> achievements = achievementDetectorUtil.extractAchievements(normalized);
        int pctAchievements = achievementDetectorUtil.countPercentageAchievements(achievements);
        int numAchievements = achievementDetectorUtil.countNumericalAchievements(achievements);
        int growthStatements = achievementDetectorUtil.countGrowthStatements(achievements);
        boolean hasAwards = achievementDetectorUtil.hasAwards(normalized);
        boolean hasOpenSource = achievementDetectorUtil.hasOpenSourceContributions(normalized);

        // Step 9: Detect contact info
        Map<String, Boolean> contactInfo = detectContactInfo(normalized);

        // Step 10: ATS-friendliness check
        boolean atsFriendly = checkAtsFriendliness(normalized);

        long processingTime = System.currentTimeMillis() - startTime;

        log.debug("NLP processing completed in {}ms - {} words, {} sections, {} skills, {} achievements",
                processingTime, normalized.split("\\s+").length,
                detectedSections.size(), extractedSkills.size(), achievements.size());

        return ProcessedResume.builder()
                .rawText(rawText)
                .normalizedText(normalized)
                .wordCount(normalized.split("\\s+").length)
                .sections(sections)
                .detectedSections(detectedSections)
                .missingSections(missingSections)
                .tokens(normalizedTokens)
                .extractedSkills(extractedSkills)
                .experienceBullets(expBullets)
                .detectedActionVerbs(detectedVerbs)
                .weakPhrasesFound(weakPhrases)
                .estimatedExperienceYears(estimatedYears)
                .achievements(achievements)
                .percentageAchievementCount(pctAchievements)
                .numericalAchievementCount(numAchievements)
                .growthStatementCount(growthStatements)
                .hasAwards(hasAwards)
                .hasOpenSource(hasOpenSource)
                .contactInfo(contactInfo)
                .atsFriendly(atsFriendly)
                .processingTimeMs(processingTime)
                .build();
    }

    // ==================== PIPELINE STEPS ====================

    /**
     * Normalize text: lowercase, clean whitespace, remove special chars
     */
    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\n{4,}", "\n\n\n")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "") // remove control chars
                .replaceAll("[\\u200B\\u00A0]", " ") // zero-width space, non-breaking space
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    /**
     * Tokenize text into words (splitting on whitespace and punctuation)
     */
    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return Arrays.stream(text.split("[\\s,;|/()\\[\\]{}:<>!?.@#$%^&*+=~`\"']+"))
                .map(String::trim)
                .filter(w -> w.length() > 1)
                .collect(Collectors.toList());
    }

    /**
     * Detect action verbs from experience bullet points
     */
    private List<String> detectActionVerbs(List<String> bulletPoints) {
        Set<String> verbs = new LinkedHashSet<>();
        for (String bullet : bulletPoints) {
            String[] words = bullet.split("\\s+");
            if (words.length > 0) {
                String first = words[0].toLowerCase().trim();
                if (actionVerbUtil.isStrongVerb(first)) {
                    verbs.add(first);
                }
            }
            // Also check all words for additional strong verbs
            for (String word : words) {
                if (actionVerbUtil.isStrongVerb(word)) {
                    verbs.add(word);
                }
            }
        }
        return new ArrayList<>(verbs);
    }

    /**
     * Estimate total years of experience from resume text
     */
    public int estimateExperienceYears(String text) {
        if (text == null || text.isBlank()) return 0;

        // Pattern 1: "X years of experience"
        Pattern yearsPattern = Pattern.compile(
                "(\\d+)\\s*\\+?\\s*years?\\s*(?:of\\s*)?(?:experience|exp|work)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = yearsPattern.matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }

        // Pattern 2: Calculate from date ranges
        Pattern dateRangePattern = Pattern.compile(
                "(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s*(\\d{4})\\s*[-–to]+\\s*(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)?[a-z]*\\s*(\\d{4}|present|current|now|till\\s*date)",
                Pattern.CASE_INSENSITIVE);
        m = dateRangePattern.matcher(text);
        int totalYears = 0;
        int currentYear = java.time.Year.now().getValue();
        while (m.find()) {
            try {
                int startYear = Integer.parseInt(m.group(1));
                String endStr = m.group(2).toLowerCase();
                int endYear = (endStr.contains("present") || endStr.contains("current")
                        || endStr.contains("now") || endStr.contains("till"))
                        ? currentYear : Integer.parseInt(endStr);
                totalYears += Math.max(0, endYear - startYear);
            } catch (NumberFormatException ignored) {}
        }
        if (totalYears > 0) return totalYears;

        // Pattern 3: Total work experience mentioned as "experience: X years"
        Pattern totalPattern = Pattern.compile(
                "(?:total|overall)\\s*(?:work\\s*)?experience\\s*:?\\s*(\\d+)\\s*\\+?\\s*years?",
                Pattern.CASE_INSENSITIVE);
        m = totalPattern.matcher(text);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }

        return 0;
    }

    /**
     * Detect contact information presence
     */
    private Map<String, Boolean> detectContactInfo(String text) {
        Map<String, Boolean> contact = new LinkedHashMap<>();

        // Email
        Pattern emailPat = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        contact.put("email", emailPat.matcher(text).find());

        // Phone (Indian and international formats)
        Pattern phonePat = Pattern.compile(
                "(?:\\+?(?:91|1|44|61|81|86|971)?[\\s\\-]?)?\\d{10,14}");
        contact.put("phone", phonePat.matcher(text.replaceAll("[\\s\\-()]", "")).find());

        // LinkedIn
        contact.put("linkedin", text.toLowerCase().contains("linkedin.com/in/"));

        // GitHub
        contact.put("github", text.toLowerCase().contains("github.com/"));

        // Portfolio
        contact.put("portfolio", text.toLowerCase().contains("portfolio")
                && (text.contains("http") || text.contains(".com") || text.contains(".me")));

        return contact;
    }

    /**
     * Check if the resume text is ATS-friendly
     */
    private boolean checkAtsFriendliness(String text) {
        if (text == null) return false;

        // Check for common ATS-unfriendly patterns
        boolean hasTableArtifacts = text.contains("||") || text.contains("__") ||
                Pattern.compile("\\|{2,}").matcher(text).find();

        // Check for header/footer artifacts
        boolean hasHeaderFooterGarbage = text.contains("Page") && text.contains("of")
                && Pattern.compile("Page\\s*\\d+\\s*(?:of|/)").matcher(text).find();

        // Check for reasonable length
        int words = text.split("\\s+").length;
        boolean reasonableLength = words >= 100 && words <= 3000;

        return !hasTableArtifacts && !hasHeaderFooterGarbage && reasonableLength;
    }

    // ==================== PROCESSED RESUME RESULT CLASS ====================

    public static class ProcessedResume {
        private final String rawText;
        private final String normalizedText;
        private final int wordCount;
        private final Map<String, String> sections;
        private final Set<String> detectedSections;
        private final List<String> missingSections;
        private final List<String> tokens;
        private final Set<String> extractedSkills;
        private final List<String> experienceBullets;
        private final List<String> detectedActionVerbs;
        private final List<String> weakPhrasesFound;
        private final int estimatedExperienceYears;
        private final List<String> achievements;
        private final int percentageAchievementCount;
        private final int numericalAchievementCount;
        private final int growthStatementCount;
        private final boolean hasAwards;
        private final boolean hasOpenSource;
        private final Map<String, Boolean> contactInfo;
        private final boolean atsFriendly;
        private final long processingTimeMs;

        private ProcessedResume(Builder builder) {
            this.rawText = builder.rawText;
            this.normalizedText = builder.normalizedText;
            this.wordCount = builder.wordCount;
            this.sections = builder.sections != null ? builder.sections : Collections.emptyMap();
            this.detectedSections = builder.detectedSections != null ? builder.detectedSections : Collections.emptySet();
            this.missingSections = builder.missingSections != null ? builder.missingSections : Collections.emptyList();
            this.tokens = builder.tokens != null ? builder.tokens : Collections.emptyList();
            this.extractedSkills = builder.extractedSkills != null ? builder.extractedSkills : Collections.emptySet();
            this.experienceBullets = builder.experienceBullets != null ? builder.experienceBullets : Collections.emptyList();
            this.detectedActionVerbs = builder.detectedActionVerbs != null ? builder.detectedActionVerbs : Collections.emptyList();
            this.weakPhrasesFound = builder.weakPhrasesFound != null ? builder.weakPhrasesFound : Collections.emptyList();
            this.estimatedExperienceYears = builder.estimatedExperienceYears;
            this.achievements = builder.achievements != null ? builder.achievements : Collections.emptyList();
            this.percentageAchievementCount = builder.percentageAchievementCount;
            this.numericalAchievementCount = builder.numericalAchievementCount;
            this.growthStatementCount = builder.growthStatementCount;
            this.hasAwards = builder.hasAwards;
            this.hasOpenSource = builder.hasOpenSource;
            this.contactInfo = builder.contactInfo != null ? builder.contactInfo : Collections.emptyMap();
            this.atsFriendly = builder.atsFriendly;
            this.processingTimeMs = builder.processingTimeMs;
        }

        public static Builder builder() { return new Builder(); }
        public static ProcessedResume empty() {
            return new Builder()
                    .wordCount(0).estimatedExperienceYears(0)
                    .percentageAchievementCount(0).numericalAchievementCount(0)
                    .growthStatementCount(0).hasAwards(false).hasOpenSource(false)
                    .atsFriendly(false).processingTimeMs(0).build();
        }

        // Getters
        public String getRawText() { return rawText; }
        public String getNormalizedText() { return normalizedText; }
        public int getWordCount() { return wordCount; }
        public Map<String, String> getSections() { return sections; }
        public Set<String> getDetectedSections() { return detectedSections; }
        public List<String> getMissingSections() { return missingSections; }
        public List<String> getTokens() { return tokens; }
        public Set<String> getExtractedSkills() { return extractedSkills; }
        public List<String> getExperienceBullets() { return experienceBullets; }
        public List<String> getDetectedActionVerbs() { return detectedActionVerbs; }
        public List<String> getWeakPhrasesFound() { return weakPhrasesFound; }
        public int getEstimatedExperienceYears() { return estimatedExperienceYears; }
        public List<String> getAchievements() { return achievements; }
        public int getPercentageAchievementCount() { return percentageAchievementCount; }
        public int getNumericalAchievementCount() { return numericalAchievementCount; }
        public int getGrowthStatementCount() { return growthStatementCount; }
        public boolean isHasAwards() { return hasAwards; }
        public boolean isHasOpenSource() { return hasOpenSource; }
        public Map<String, Boolean> getContactInfo() { return contactInfo; }
        public boolean isAtsFriendly() { return atsFriendly; }
        public long getProcessingTimeMs() { return processingTimeMs; }

        public static class Builder {
            private String rawText;
            private String normalizedText;
            private int wordCount;
            private Map<String, String> sections;
            private Set<String> detectedSections;
            private List<String> missingSections;
            private List<String> tokens;
            private Set<String> extractedSkills;
            private List<String> experienceBullets;
            private List<String> detectedActionVerbs;
            private List<String> weakPhrasesFound;
            private int estimatedExperienceYears;
            private List<String> achievements;
            private int percentageAchievementCount;
            private int numericalAchievementCount;
            private int growthStatementCount;
            private boolean hasAwards;
            private boolean hasOpenSource;
            private Map<String, Boolean> contactInfo;
            private boolean atsFriendly;
            private long processingTimeMs;

            public Builder rawText(String v) { rawText = v; return this; }
            public Builder normalizedText(String v) { normalizedText = v; return this; }
            public Builder wordCount(int v) { wordCount = v; return this; }
            public Builder sections(Map<String, String> v) { sections = v; return this; }
            public Builder detectedSections(Set<String> v) { detectedSections = v; return this; }
            public Builder missingSections(List<String> v) { missingSections = v; return this; }
            public Builder tokens(List<String> v) { tokens = v; return this; }
            public Builder extractedSkills(Set<String> v) { extractedSkills = v; return this; }
            public Builder experienceBullets(List<String> v) { experienceBullets = v; return this; }
            public Builder detectedActionVerbs(List<String> v) { detectedActionVerbs = v; return this; }
            public Builder weakPhrasesFound(List<String> v) { weakPhrasesFound = v; return this; }
            public Builder estimatedExperienceYears(int v) { estimatedExperienceYears = v; return this; }
            public Builder achievements(List<String> v) { achievements = v; return this; }
            public Builder percentageAchievementCount(int v) { percentageAchievementCount = v; return this; }
            public Builder numericalAchievementCount(int v) { numericalAchievementCount = v; return this; }
            public Builder growthStatementCount(int v) { growthStatementCount = v; return this; }
            public Builder hasAwards(boolean v) { hasAwards = v; return this; }
            public Builder hasOpenSource(boolean v) { hasOpenSource = v; return this; }
            public Builder contactInfo(Map<String, Boolean> v) { contactInfo = v; return this; }
            public Builder atsFriendly(boolean v) { atsFriendly = v; return this; }
            public Builder processingTimeMs(long v) { processingTimeMs = v; return this; }
            public ProcessedResume build() { return new ProcessedResume(this); }
        }
    }
}
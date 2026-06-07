package com.resumatchpro.utility;

import com.resumatchpro.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.AbstractMap;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ScoringEngineUtil {

    private static final Logger log = LoggerFactory.getLogger(ScoringEngineUtil.class);

    private final KeywordExtractorUtil keywordExtractor;
    private final SectionDetectorUtil sectionDetector;
    private final ActionVerbUtil actionVerbUtil;
    private final AchievementDetectorUtil achievementDetector;
    private final SkillSynonymUtil skillSynonymUtil;
    private final GradeCalculatorUtil gradeCalculator;

    public ScoringEngineUtil(KeywordExtractorUtil keywordExtractor,
                             SkillSynonymUtil skillSynonymUtil,
                             SectionDetectorUtil sectionDetector,
                             ActionVerbUtil actionVerbUtil,
                             AchievementDetectorUtil achievementDetector,
                             GradeCalculatorUtil gradeCalculator) {
        this.keywordExtractor = keywordExtractor;
        this.sectionDetector = sectionDetector;
        this.actionVerbUtil = actionVerbUtil;
        this.achievementDetector = achievementDetector;
        this.skillSynonymUtil = skillSynonymUtil;
        this.gradeCalculator = gradeCalculator;
    }

    /**
     * Score a processed resume against a specific job listing.
     * Returns complete score breakdown with detailed analysis.
     */
    public ScoreResult score(NLPProcessorUtil.ProcessedResume resume, JobListing job) {
        long startTime = System.currentTimeMillis();

        // Parse job requirements
        List<String> atsKeywords = parseCommaSeparated(job.getAtsKeywords());
        List<String> requiredSkills = parseCommaSeparated(job.getRequiredSkills());
        List<String> preferredSkills = parseCommaSeparated(job.getPreferredSkills());
        int minYearsExp = job.getMinExperienceYears() != null ? job.getMinExperienceYears() : 0;

        // Build each score
        KeywordScore kwScore = scoreKeywordMatch(resume, atsKeywords);
        SkillScore skillScore = scoreSkillRelevance(resume, requiredSkills, preferredSkills);
        ExperienceScore expScore = scoreExperienceQuality(resume, minYearsExp);
        AchievementScore achScore = scoreAchievements(resume);
        FormattingScore fmtScore = scoreFormatting(resume);
        EducationScore eduScore = scoreEducation(resume, job.getEducationRequirement());

        // Calculate total (raw max = 105 due to possible bonuses)
        int rawTotal = kwScore.score + skillScore.score + expScore.score +
                       achScore.score + fmtScore.score + eduScore.score;
        int finalScore = Math.min(100, Math.round(rawTotal));

        // Grade calculation
        GradeCalculatorUtil.GradeResult grade = gradeCalculator.calculateGrade(finalScore);

        // Build weak areas and suggestions
        List<String> weakAreas = buildWeakAreas(resume, kwScore, skillScore, expScore,
                achScore, fmtScore, eduScore);
        List<ImprovementSuggestion> suggestions = buildSuggestions(resume, kwScore,
                skillScore, expScore, achScore, fmtScore, eduScore, atsKeywords);
        String overallFeedback = buildOverallFeedback(finalScore, grade, kwScore, skillScore, expScore);

        long processingTimeMs = System.currentTimeMillis() - startTime;

        return new ScoreResult(finalScore, kwScore, skillScore, expScore, achScore,
                fmtScore, eduScore, grade, weakAreas, suggestions, overallFeedback,
                processingTimeMs, resume, requiredSkills, preferredSkills, atsKeywords);
    }

    // ==================== 1. KEYWORD MATCH SCORE (30 pts) ====================

    private KeywordScore scoreKeywordMatch(NLPProcessorUtil.ProcessedResume resume,
                                            List<String> atsKeywords) {
        if (atsKeywords == null || atsKeywords.isEmpty()) {
            return new KeywordScore(30, 30, 100, "GOOD",
                    Collections.emptySet(), Collections.emptySet(), Collections.emptyMap());
        }

        Map<String, Double> confidenceScores = new LinkedHashMap<>();
        Set<String> matched = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();
        double totalScore = 0;

        Map<String, String> sections = resume.getSections();
        String fullText = resume.getNormalizedText();

        for (String kw : atsKeywords) {
            if (kw == null || kw.isBlank()) continue;
            String cleanKw = kw.trim().toLowerCase();

            boolean found = keywordExtractor.containsKeyword(fullText, cleanKw);
            if (found) {
                matched.add(cleanKw);

                // Section-based weight
                double sectionWeight = keywordExtractor.getSectionWeight(cleanKw, sections);

                // Frequency weight
                int freq = keywordExtractor.countFrequency(fullText, cleanKw);
                double freqWeight = keywordExtractor.getFrequencyWeight(freq);

                // Also check synonym match (partial credit)
                double confidence = sectionWeight * freqWeight;
                confidenceScores.put(cleanKw, Math.min(1.0, confidence));
                totalScore += confidence;

            } else {
                // Check for synonym match (0.8 credit)
                boolean synonymMatch = false;
                for (Map.Entry<String, String> syn : getSynonymEntries(cleanKw)) {
                    if (keywordExtractor.containsKeyword(fullText, syn.getKey())) {
                        matched.add(cleanKw + " (via " + syn.getKey() + ")");
                        double sectionWeight = keywordExtractor.getSectionWeight(syn.getKey(), sections);
                        double synConfidence = sectionWeight * 0.8;
                        confidenceScores.put(cleanKw, synConfidence);
                        totalScore += synConfidence;
                        synonymMatch = true;
                        break;
                    }
                }
                if (!synonymMatch) {
                    missing.add(cleanKw);
                    confidenceScores.put(cleanKw, 0.0);
                }
            }
        }

        int effectiveTotal = atsKeywords.size();
        double scaledScore = effectiveTotal > 0 ? (totalScore / effectiveTotal) * 30 : 30;
        int score = Math.min(30, (int) Math.round(scaledScore));
        int percentage = effectiveTotal > 0
                ? (int) Math.round((double) matched.size() / effectiveTotal * 100) : 100;

        String status = percentage >= 70 ? "GOOD" : percentage >= 40 ? "AVERAGE" : "POOR";

        return new KeywordScore(score, 30, percentage, status, matched, missing, confidenceScores);
    }

    // ==================== 2. SKILL RELEVANCE SCORE (25 pts) ====================

    private SkillScore scoreSkillRelevance(NLPProcessorUtil.ProcessedResume resume,
                                            List<String> requiredSkills,
                                            List<String> preferredSkills) {
        Set<String> extractedSkills = resume.getExtractedSkills();
        String fullText = resume.getNormalizedText();

        // Required skills (70% = 17.5 pts)
        Set<String> matchedRequired = new LinkedHashSet<>();
        Set<String> missingRequired = new LinkedHashSet<>();

        if (requiredSkills != null) {
            for (String skill : requiredSkills) {
                if (skill == null || skill.isBlank()) continue;
                String cleanSkill = skill.trim().toLowerCase();

                if (extractedSkills.contains(cleanSkill)
                        || keywordExtractor.containsKeyword(fullText, cleanSkill)) {
                    matchedRequired.add(skill);
                } else {
                    // Check synonym
                    boolean found = false;
                    for (Map.Entry<String, String> syn : getSynonymEntries(cleanSkill)) {
                        if (extractedSkills.contains(syn.getKey())
                                || keywordExtractor.containsKeyword(fullText, syn.getKey())) {
                            matchedRequired.add(skill);
                            found = true;
                            break;
                        }
                    }
                    if (!found) missingRequired.add(skill);
                }
            }
        }

        int totalReq = (requiredSkills != null && !requiredSkills.isEmpty()) ? requiredSkills.size() : 1;
        double reqRatio = (double) matchedRequired.size() / totalReq;
        double reqScore = reqRatio * 17.5;

        // Preferred skills (30% = 7.5 pts)
        Set<String> matchedPreferred = new LinkedHashSet<>();
        Set<String> missingPreferred = new LinkedHashSet<>();

        if (preferredSkills != null) {
            for (String skill : preferredSkills) {
                if (skill == null || skill.isBlank()) continue;
                String cleanSkill = skill.trim().toLowerCase();

                if (extractedSkills.contains(cleanSkill)
                        || keywordExtractor.containsKeyword(fullText, cleanSkill)) {
                    matchedPreferred.add(skill);
                } else {
                    missingPreferred.add(skill);
                }
            }
        }

        int totalPref = (preferredSkills != null && !preferredSkills.isEmpty())
                ? preferredSkills.size() : 1;
        double prefRatio = (double) matchedPreferred.size() / totalPref;
        double prefScore = prefRatio * 7.5;

        // Skill density bonus
        int uniqueSkills = extractedSkills.size();
        double bonus = 0;
        if (uniqueSkills > 25) bonus = 1.5;
        else if (uniqueSkills > 15) bonus = 1.0;

        // Certification bonus (capped)
        double certBonus = 0;
        if (resume.getDetectedSections() != null
                && resume.getDetectedSections().contains("CERTIFICATIONS")) {
            certBonus = Math.min(1.0, matchedRequired.size() * 0.5);
        }

        double total = reqScore + prefScore + bonus + certBonus;
        int score = Math.min(25, (int) Math.round(total));

        int matchPct = (int) Math.round(((double) (matchedRequired.size() + matchedPreferred.size())
                / Math.max(1, totalReq + totalPref)) * 100);

        String status = matchPct >= 70 ? "GOOD" : matchPct >= 40 ? "AVERAGE" : "POOR";

        return new SkillScore(score, 25, matchPct, status,
                matchedRequired, missingRequired, matchedPreferred, missingPreferred, uniqueSkills);
    }

    // ==================== 3. EXPERIENCE QUALITY SCORE (20 pts) ====================

    private ExperienceScore scoreExperienceQuality(NLPProcessorUtil.ProcessedResume resume,
                                                    int minYearsRequired) {
        // Action verb score (8 pts)
        List<String> verbs = resume.getDetectedActionVerbs();
        double verbScore = Math.min(8, verbs.size() * 0.5);

        // Penalize weak phrases
        int weakCount = resume.getWeakPhrasesFound() != null ? resume.getWeakPhrasesFound().size() : 0;
        verbScore -= (weakCount * 0.5);
        verbScore = Math.max(0, verbScore);

        // Technical depth (6 pts)
        double techDepth = 0;
        String expContent = resume.getSections() != null
                ? resume.getSections().getOrDefault("EXPERIENCE", "") : "";

        if (actionVerbUtil.hasArchitectureTerms(expContent)) techDepth += 2;
        if (actionVerbUtil.hasLeadershipTerms(expContent)) techDepth += 2;

        // Count technical terms in experience
        Set<String> techSkills = resume.getExtractedSkills();
        long techInExp = techSkills.stream()
                .filter(s -> expContent.toLowerCase().contains(s.toLowerCase()))
                .count();
        techDepth += Math.min(2, techInExp * 0.3);

        techDepth = Math.min(6, techDepth);

        // Experience years match (6 pts)
        int estimatedYears = resume.getEstimatedExperienceYears();
        int gap = minYearsRequired - estimatedYears;
        double yearsScore;
        if (minYearsRequired == 0) {
            yearsScore = 6; // Entry-level job
        } else if (gap <= 0) {
            yearsScore = 6; // Meets or exceeds
        } else if (gap <= 1) {
            yearsScore = 4;
        } else if (gap <= 2) {
            yearsScore = 2;
        } else {
            yearsScore = 0;
        }

        // No experience section penalty
        if (resume.getDetectedSections() == null
                || !resume.getDetectedSections().contains("EXPERIENCE")) {
            yearsScore = 0;
            techDepth = 0;
            if (resume.getMissingSections() != null
                    && resume.getMissingSections().contains("Experience")) {
                techDepth = 0;
            }
        }

        int score = Math.min(20, (int) Math.round(verbScore + techDepth + yearsScore));
        String status = score >= 15 ? "GOOD" : score >= 10 ? "AVERAGE" : "POOR";

        return new ExperienceScore(score, 20, (int) Math.round(verbScore),
                techDepth, (int) Math.round(yearsScore), estimatedYears,
                minYearsRequired, gap > 0 ? gap : 0, status, verbs, resume.getWeakPhrasesFound());
    }

    // ==================== 4. ACHIEVEMENTS & IMPACT SCORE (15 pts) ====================

    private AchievementScore scoreAchievements(NLPProcessorUtil.ProcessedResume resume) {
        int pctCount = resume.getPercentageAchievementCount();
        int numCount = resume.getNumericalAchievementCount();
        int growthCount = resume.getGrowthStatementCount();
        boolean hasAwards = resume.isHasAwards();
        boolean hasOpenSource = resume.isHasOpenSource();

        // Percentage achievements (5 pts)
        double pctScore = Math.min(5, pctCount * 1.5);

        // Numerical achievements (5 pts)
        double numScore = Math.min(5, numCount * 1.0);

        // Growth statements (5 pts)
        double growthScore = Math.min(5, growthCount * 1.5);

        // Awards bonus (1 pt)
        double awardBonus = hasAwards ? 1 : 0;

        // Open source bonus (1 pt)
        double ossBonus = hasOpenSource ? 1 : 0;

        int score = Math.min(15, (int) Math.round(pctScore + numScore + growthScore + awardBonus + ossBonus));
        String status = score >= 12 ? "GOOD" : score >= 7 ? "AVERAGE" : "POOR";

        return new AchievementScore(score, 15, pctCount, numCount, growthCount,
                hasAwards, hasOpenSource, status, resume.getAchievements());
    }

    // ==================== 5. FORMATTING & STRUCTURE SCORE (10 pts) ====================

    private FormattingScore scoreFormatting(NLPProcessorUtil.ProcessedResume resume) {
        double score = 0;
        Set<String> sections = resume.getDetectedSections();

        // Section presence (6 pts)
        if (sections != null) {
            if (sections.contains("SKILLS")) score += 1.5;
            if (sections.contains("EXPERIENCE")) score += 1.5;
            if (sections.contains("EDUCATION")) score += 1.5;
            if (sections.contains("PROJECTS")) score += 1.0;
            if (sections.contains("SUMMARY")) score += 0.5;
            if (sections.contains("CERTIFICATIONS")) score += 0.5;
        }

        // Contact info (2 pts)
        Map<String, Boolean> contact = resume.getContactInfo();
        if (contact != null) {
            if (Boolean.TRUE.equals(contact.get("email"))) score += 0.5;
            if (Boolean.TRUE.equals(contact.get("phone"))) score += 0.5;
            if (Boolean.TRUE.equals(contact.get("linkedin"))) score += 0.5;
            if (Boolean.TRUE.equals(contact.get("github"))) score += 0.5;
        }

        // ATS friendliness (2 pts)
        if (resume.isAtsFriendly()) score += 1.5;
        int wordCount = resume.getWordCount();
        if (wordCount >= 300 && wordCount <= 3000) score += 0.5;

        // Penalties
        if (sections != null) {
            if (!sections.contains("SKILLS")) score -= 2;
            if (!sections.contains("EXPERIENCE")) score -= 2;
            if (!sections.contains("EDUCATION")) score -= 1;
        }
        if (wordCount < 100) score -= 2;

        score = Math.max(0, Math.min(10, score));

        int finalScore = (int) Math.round(score);
        String status = finalScore >= 8 ? "GOOD" : finalScore >= 5 ? "AVERAGE" : "POOR";

        return new FormattingScore(finalScore, 10, sections != null ? sections : Collections.emptySet(),
                resume.getMissingSections(), resume.getContactInfo(),
                resume.isAtsFriendly(), wordCount, status);
    }

    // ==================== 6. EDUCATION MATCH SCORE (5 pts) ====================

    private EducationScore scoreEducation(NLPProcessorUtil.ProcessedResume resume,
                                           String requiredEducation) {
        if (requiredEducation == null || requiredEducation.isBlank()) {
            return new EducationScore(5, 5, "N/A", "N/A", "NO_REQUIREMENT", "GOOD");
        }

        String fullText = resume.getNormalizedText();
        String educationSection = resume.getSections() != null
                ? resume.getSections().getOrDefault("EDUCATION", "") : "";

        String allEduText = (educationSection + " " + fullText).toLowerCase();
        String reqLower = requiredEducation.toLowerCase();

        // Extract education levels
        String detectedEdu = detectEducation(allEduText);

        int score;
        String matchType;

        if (allEduText.contains(reqLower)) {
            score = 5;
            matchType = "EXACT_MATCH";
        } else if (isRelatedEducation(reqLower, allEduText)) {
            score = 4;
            matchType = "RELATED_MATCH";
        } else if (isHigherQualification(reqLower, allEduText)) {
            score = 5;
            matchType = "HIGHER_QUALIFICATION";
        } else if (isAcceptableAlternative(reqLower, allEduText)) {
            score = 3;
            matchType = "ACCEPTABLE_ALTERNATIVE";
        } else if (detectedEdu.equals("NONE")) {
            score = 1;
            matchType = "NOT_FOUND";
        } else {
            score = 1;
            matchType = "NOT_MATCHED";
        }

        // Certification override
        if (score <= 3 && resume.getDetectedSections() != null
                && resume.getDetectedSections().contains("CERTIFICATIONS")) {
            score = Math.min(5, score + 1);
            matchType += "_WITH_CERTS";
        }

        String status = score >= 4 ? "GOOD" : score >= 2 ? "AVERAGE" : "POOR";

        return new EducationScore(score, 5, detectedEdu, requiredEducation, matchType, status);
    }

    // ==================== HELPERS ====================

    private List<String> parseCommaSeparated(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<Map.Entry<String, String>> getSynonymEntries(String term) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        if (term == null) return entries;
        String lower = term.toLowerCase().trim();
        for (Map.Entry<String, String> e : skillSynonymUtil.getSynonymMap().entrySet()) {
            if (e.getValue().equalsIgnoreCase(lower) || e.getKey().equalsIgnoreCase(lower)) {
                entries.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
            }
        }
        return entries;
    }

    private String detectEducation(String text) {
        if (text.contains("b.tech") || text.contains("bachelor of technology")) return "B.Tech";
        if (text.contains("b.e.") || text.contains("bachelor of engineering")) return "B.E.";
        if (text.contains("m.tech") || text.contains("master of technology")) return "M.Tech";
        if (text.contains("mca") || text.contains("master of computer application")) return "MCA";
        if (text.contains("mba") || text.contains("master of business")) return "MBA";
        if (text.contains("bca") || text.contains("bachelor of computer")) return "BCA";
        if (text.contains("b.sc") || text.contains("bachelor of science")) return "B.Sc";
        if (text.contains("m.sc") || text.contains("master of science")) return "M.Sc";
        if (text.contains("ph.d") || text.contains("doctorate")) return "Ph.D";
        if (text.contains("diploma")) return "Diploma";
        if (text.contains("bachelor") || text.contains("undergraduate")) return "Bachelor's";
        if (text.contains("master") || text.contains("postgraduate")) return "Master's";
        return "UNKNOWN";
    }

    private boolean isRelatedEducation(String required, String text) {
        String req = required.toLowerCase();
        String t = text.toLowerCase();
        // CS, IT, CE, SE are related
        if ((req.contains("computer") || req.contains("cs") || req.contains("cse"))
                && (t.contains("information technology") || t.contains("it") ||
                    t.contains("computer") || t.contains("software"))) return true;
        if (req.contains("information") && (t.contains("computer") || t.contains("cs") ||
                t.contains("cse") || t.contains("software"))) return true;
        return false;
    }

    private boolean isHigherQualification(String required, String text) {
        String t = text.toLowerCase();
        boolean hasHigher = t.contains("m.tech") || t.contains("mca") || t.contains("mba")
                || t.contains("m.sc") || t.contains("ph.d") || t.contains("master");
        boolean reqIsBachelors = required.toLowerCase().contains("b.tech")
                || required.toLowerCase().contains("b.e.") || required.toLowerCase().contains("bachelor");
        return reqIsBachelors && hasHigher;
    }

    private boolean isAcceptableAlternative(String required, String text) {
        String t = text.toLowerCase();
        boolean hasBcaOrBsc = t.contains("bca") || t.contains("b.sc")
                || t.contains("bachelor of science") || t.contains("bachelor of computer");
        boolean reqIsBTechOrBE = required.toLowerCase().contains("b.tech")
                || required.toLowerCase().contains("b.e.");
        return reqIsBTechOrBE && hasBcaOrBsc;
    }

    // ==================== WEAK AREAS & SUGGESTIONS ====================

    private List<String> buildWeakAreas(NLPProcessorUtil.ProcessedResume resume,
                                         KeywordScore kw, SkillScore skill, ExperienceScore exp,
                                         AchievementScore ach, FormattingScore fmt, EducationScore edu) {
        List<String> weakAreas = new ArrayList<>();

        if (kw.percentage < 50) weakAreas.add("Low keyword match rate ("
                + kw.percentage + "%). Missing critical ATS keywords required by this job.");
        if (kw.missingKeywords != null && !kw.missingKeywords.isEmpty()) {
            weakAreas.add("Missing keywords: " + String.join(", ", kw.missingKeywords));
        }
        if (skill.missingRequired != null && !skill.missingRequired.isEmpty()) {
            weakAreas.add("Missing required skills: " + String.join(", ", skill.missingRequired));
        }
        if (exp.weakPhrases != null && !exp.weakPhrases.isEmpty()) {
            weakAreas.add("Experience descriptions contain weak phrases like '"
                    + String.join("', '", exp.weakPhrases.stream().limit(2).collect(Collectors.toList()))
                    + "'. Replace with strong action verbs.");
        }
        if (exp.estimatedYears < exp.requiredYears && exp.requiredYears > 0) {
            weakAreas.add("Experience gap: " + exp.estimatedYears + " years detected vs. "
                    + exp.requiredYears + " years required.");
        }
        if (ach.totalAchievements() < 2) {
            weakAreas.add("Few measurable achievements detected. Add numbers and percentages.");
        }
        if (fmt.missingSections != null && !fmt.missingSections.isEmpty()) {
            weakAreas.add("Missing resume sections: " + String.join(", ", fmt.missingSections));
        }
        if (fmt.contactInfo != null) {
            if (!Boolean.TRUE.equals(fmt.contactInfo.get("github"))) {
                weakAreas.add("GitHub profile URL not present in resume.");
            }
            if (!Boolean.TRUE.equals(fmt.contactInfo.get("linkedin"))) {
                weakAreas.add("LinkedIn profile URL not present in resume.");
            }
        }
        if (!fmt.atsFriendly) {
            weakAreas.add("Resume may not be ATS-friendly. Check for tables, columns, or graphics.");
        }
        if (resume.getWordCount() < 300) {
            weakAreas.add("Resume is short (" + resume.getWordCount()
                    + " words). Consider adding more detail.");
        }

        return weakAreas;
    }

    private List<ImprovementSuggestion> buildSuggestions(NLPProcessorUtil.ProcessedResume resume,
                                                          KeywordScore kw, SkillScore skill,
                                                          ExperienceScore exp, AchievementScore ach,
                                                          FormattingScore fmt, EducationScore edu,
                                                          List<String> atsKeywords) {
        List<ImprovementSuggestion> suggestions = new ArrayList<>();
        int priority = 1;

        // Missing ATS keywords
        if (kw.missingKeywords != null && !kw.missingKeywords.isEmpty()) {
            suggestions.add(new ImprovementSuggestion(priority++, "Missing Keywords",
                    "Add these keywords to your resume: " + String.join(", ", kw.missingKeywords)
                    + ". Even basic familiarity should be mentioned explicitly.",
                    "HIGH", 6));
        }

        // Missing required skills
        if (skill.missingRequired != null && !skill.missingRequired.isEmpty()) {
            suggestions.add(new ImprovementSuggestion(priority++, "Missing Skills",
                    "Add " + String.join(", ", skill.missingRequired)
                    + " to your skills section — even if self-learned.",
                    "HIGH", 4));
        }

        // Weak phrases
        if (exp.weakPhrases != null && !exp.weakPhrases.isEmpty()) {
            suggestions.add(new ImprovementSuggestion(priority++, "Experience Quality",
                    "Replace '" + String.join("', '", exp.weakPhrases.stream().limit(2)
                            .collect(Collectors.toList()))
                    + "' with specific, measurable accomplishments using strong action verbs.",
                    "HIGH", 4));
        }

        // Achievements
        if (ach.totalAchievements() < 3) {
            suggestions.add(new ImprovementSuggestion(priority++, "Achievements",
                    "Add at least 2-3 measurable outcomes to your experience. "
                    + "Example: 'Reduced API response time by 35% through query optimization'",
                    "MEDIUM", 3));
        }

        // Contact info
        if (fmt.contactInfo != null && (!Boolean.TRUE.equals(fmt.contactInfo.get("github"))
                || !Boolean.TRUE.equals(fmt.contactInfo.get("linkedin")))) {
            List<String> missing = new ArrayList<>();
            if (!Boolean.TRUE.equals(fmt.contactInfo.get("github"))) missing.add("GitHub");
            if (!Boolean.TRUE.equals(fmt.contactInfo.get("linkedin"))) missing.add("LinkedIn");
            suggestions.add(new ImprovementSuggestion(priority++, "Contact & Links",
                    "Add " + String.join(" and ", missing)
                    + " profile URL to improve recruiter trust and ATS structure score.",
                    "LOW", 1));
        }

        // Missing sections
        if (fmt.missingSections != null && !fmt.missingSections.isEmpty()) {
            String firstMissing = fmt.missingSections.get(0);
            suggestions.add(new ImprovementSuggestion(priority++, "Resume Structure",
                    "Add a dedicated '" + firstMissing + "' section to your resume.",
                    "MEDIUM", 2));
        }

        return suggestions;
    }

    private String buildOverallFeedback(int score, GradeCalculatorUtil.GradeResult grade,
                                         KeywordScore kw, SkillScore skill, ExperienceScore exp) {
        if (score >= 80) {
            return "Your resume is a strong match for this position! "
                    + "Focus on preparing for interviews and highlighting the matched skills.";
        } else if (score >= 70) {
            return "Your resume is a good match with room for improvement. "
                    + "Focus on addressing the missing keywords and strengthening your experience descriptions.";
        } else if (score >= 50) {
            return "Your resume partially matches the job requirements. "
                    + "Significant improvements are needed in keyword matching and skill alignment.";
        } else {
            return "Your resume needs substantial improvement to pass ATS screening for this role. "
                    + "Focus on adding the required keywords, skills, and making your experience quantifiable.";
        }
    }

    // ==================== SCORE RESULT CLASSES ====================

    public static class ScoreResult {
        public final int finalScore;
        public final KeywordScore keywordMatch;
        public final SkillScore skillRelevance;
        public final ExperienceScore experienceQuality;
        public final AchievementScore achievements;
        public final FormattingScore formatting;
        public final EducationScore education;
        public final GradeCalculatorUtil.GradeResult grade;
        public final List<String> weakAreas;
        public final List<ImprovementSuggestion> suggestions;
        public final String overallFeedback;
        public final long processingTimeMs;
        public final NLPProcessorUtil.ProcessedResume processedResume;
        public final List<String> requiredSkills;
        public final List<String> preferredSkills;
        public final List<String> atsKeywords;

        public ScoreResult(int finalScore, KeywordScore keywordMatch, SkillScore skillRelevance,
                           ExperienceScore experienceQuality, AchievementScore achievements,
                           FormattingScore formatting, EducationScore education,
                           GradeCalculatorUtil.GradeResult grade, List<String> weakAreas,
                           List<ImprovementSuggestion> suggestions, String overallFeedback,
                           long processingTimeMs, NLPProcessorUtil.ProcessedResume processedResume,
                           List<String> requiredSkills, List<String> preferredSkills,
                           List<String> atsKeywords) {
            this.finalScore = finalScore;
            this.keywordMatch = keywordMatch;
            this.skillRelevance = skillRelevance;
            this.experienceQuality = experienceQuality;
            this.achievements = achievements;
            this.formatting = formatting;
            this.education = education;
            this.grade = grade;
            this.weakAreas = weakAreas;
            this.suggestions = suggestions;
            this.overallFeedback = overallFeedback;
            this.processingTimeMs = processingTimeMs;
            this.processedResume = processedResume;
            this.requiredSkills = requiredSkills;
            this.preferredSkills = preferredSkills;
            this.atsKeywords = atsKeywords;
        }
    }

    // ==================== SCORE BREAKDOWN RECORDS ====================

    public static class KeywordScore {
        public final int score, maxScore, percentage;
        public final String status;
        public final Set<String> matchedKeywords, missingKeywords;
        public final Map<String, Double> confidenceScores;
        public KeywordScore(int s, int m, int p, String st, Set<String> mk, Set<String> ms, Map<String, Double> cs) {
            score = s; maxScore = m; percentage = p; status = st;
            matchedKeywords = mk; missingKeywords = ms; confidenceScores = cs;
        }
    }

    public static class SkillScore {
        public final int score, maxScore, matchPercentage;
        public final String status;
        public final Set<String> matchedRequired, missingRequired, matchedPreferred, missingPreferred;
        public final int uniqueSkills;
        public SkillScore(int s, int m, int mp, String st, Set<String> mr, Set<String> msR,
                          Set<String> mpf, Set<String> msP, int us) {
            score = s; maxScore = m; matchPercentage = mp; status = st;
            matchedRequired = mr; missingRequired = msR;
            matchedPreferred = mpf; missingPreferred = msP; uniqueSkills = us;
        }
    }

    public static class ExperienceScore {
        public final int score, maxScore, actionVerbScore, techDepthScore, yearsScore;
        public final int estimatedYears, requiredYears, gapYears;
        public final String status;
        public final List<String> detectedVerbs, weakPhrases;
        public ExperienceScore(int s, int m, int av, double td, int ys, int ey, int ry, int gy,
                               String st, List<String> dv, List<String> wp) {
            score = s; maxScore = m; actionVerbScore = av; techDepthScore = (int) td;
            yearsScore = ys; estimatedYears = ey; requiredYears = ry; gapYears = gy;
            status = st; detectedVerbs = dv; weakPhrases = wp;
        }
    }

    public static class AchievementScore {
        public final int score, maxScore, pctCount, numCount, growthCount;
        public final boolean hasAwards, hasOpenSource;
        public final String status;
        public final List<String> achievements;
        public AchievementScore(int s, int m, int pc, int nc, int gc, boolean ha, boolean ho,
                                String st, List<String> ach) {
            score = s; maxScore = m; pctCount = pc; numCount = nc; growthCount = gc;
            hasAwards = ha; hasOpenSource = ho; status = st; achievements = ach;
        }
        public int totalAchievements() { return achievements != null ? achievements.size() : 0; }
    }

    public static class FormattingScore {
        public final int score, maxScore;
        public final Set<String> detectedSections;
        public final List<String> missingSections;
        public final Map<String, Boolean> contactInfo;
        public final boolean atsFriendly;
        public final int wordCount;
        public final String status;
        public FormattingScore(int s, int m, Set<String> ds, List<String> ms,
                               Map<String, Boolean> ci, boolean af, int wc, String st) {
            score = s; maxScore = m; detectedSections = ds; missingSections = ms;
            contactInfo = ci; atsFriendly = af; wordCount = wc; status = st;
        }
    }

    public static class EducationScore {
        public final int score, maxScore;
        public final String detected, required, matchType, status;
        public EducationScore(int s, int m, String d, String r, String mt, String st) {
            score = s; maxScore = m; detected = d; required = r; matchType = mt; status = st;
        }
    }

    public static class ImprovementSuggestion {
        public final int priority;
        public final String area, suggestion, impact;
        public final int estimatedScoreGain;
        public ImprovementSuggestion(int p, String a, String s, String i, int esg) {
            priority = p; area = a; suggestion = s; impact = i; estimatedScoreGain = esg;
        }
    }
}
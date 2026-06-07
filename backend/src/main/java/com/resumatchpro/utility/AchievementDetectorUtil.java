package com.resumatchpro.utility;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

@Component
public class AchievementDetectorUtil {

    // Percentage patterns: "improved performance by 40%", "reduced by 25%"
    private static final Pattern PERCENTAGE_PATTERN =
            Pattern.compile("\\b(\\d{1,3}(?:\\.\\d+)?)\\s*%");

    // Large number patterns: "10,000+ users", "1M transactions", "$50K revenue"
    private static final Pattern LARGE_NUMBER_PATTERN =
            Pattern.compile("\\b(\\d{1,3}(?:,\\d{3})+)\\b");

    private static final Pattern NUMBER_WITH_SUFFIX =
            Pattern.compile("\\b(\\d+(?:\\.\\d+)?)\\s*([KkMmBb])\\b");

    // Dollar amounts
    private static final Pattern DOLLAR_PATTERN =
            Pattern.compile("\\$\\s*(\\d+(?:,\\d{3})*(?:\\.\\d+)?)\\s*([KkMmBb]?)");

    // Growth/impact verbs near numbers
    private static final Set<String> GROWTH_VERBS = new HashSet<>(Arrays.asList(
        "increased", "reduced", "improved", "saved", "delivered",
        "achieved", "generated", "decreased", "boosted", "enhanced",
        "accelerated", "grew", "expanded", "cut", "lowered",
        "raised", "surpassed", "exceeded", "drove", "produced",
        "earned", "acquired", "converted", "retained", "onboarded"
    ));

    // Scale/user patterns
    private static final Pattern USER_SCALE_PATTERN =
            Pattern.compile("\\b(\\d+(?:,\\d{3})*(?:\\.\\d+)?)\\s*(?:million|billion|thousand)?\\s*(?:users|clients|customers|requests|transactions|downloads|subscribers|visitors|students|employees)\\b",
                    Pattern.CASE_INSENSITIVE);

    // Time-based achievements
    private static final Pattern TIME_ACHIEVEMENT =
            Pattern.compile("\\b(?:within|under|in just|over|across|spanning)\\s*(\\d+)\\s*(?:days?|weeks?|months?|years?|hours?|minutes?)\\b",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Extract all measurable achievements from resume text.
     * Returns list of achievement strings.
     */
    public List<String> extractAchievements(String text) {
        List<String> achievements = new ArrayList<>();
        if (text == null || text.isBlank()) return achievements;

        // Split into bullet points / sentences
        String[] sentences = text.split("[.\\n•\\-*]");

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.length() < 10) continue;

            boolean isAchievement = false;
            StringBuilder achievement = new StringBuilder();

            // Check for percentage
            Matcher pctMatcher = PERCENTAGE_PATTERN.matcher(trimmed);
            if (pctMatcher.find()) {
                isAchievement = true;
                achievement.append(trimmed);
            }

            // Check for large/impressive numbers
            Matcher numMatcher = LARGE_NUMBER_PATTERN.matcher(trimmed);
            if (numMatcher.find()) {
                isAchievement = true;
                if (achievement.length() == 0) achievement.append(trimmed);
            }

            // Check for dollar amounts
            Matcher dollarMatcher = DOLLAR_PATTERN.matcher(trimmed);
            if (dollarMatcher.find()) {
                isAchievement = true;
                if (achievement.length() == 0) achievement.append(trimmed);
            }

            // Check for growth verbs near numbers
            String lower = trimmed.toLowerCase();
            for (String verb : GROWTH_VERBS) {
                if (lower.contains(verb)) {
                    // Check if there's also a number nearby
                    if (Pattern.compile("\\d+").matcher(trimmed).find()) {
                        isAchievement = true;
                        if (achievement.length() == 0) achievement.append(trimmed);
                        break;
                    }
                }
            }

            // Check for user/scale patterns
            Matcher userMatcher = USER_SCALE_PATTERN.matcher(trimmed);
            if (userMatcher.find()) {
                isAchievement = true;
                if (achievement.length() == 0) achievement.append(trimmed);
            }

            // Time-based achievement
            Matcher timeMatcher = TIME_ACHIEVEMENT.matcher(trimmed);
            if (timeMatcher.find()) {
                isAchievement = true;
                if (achievement.length() == 0) achievement.append(trimmed);
            }

            if (isAchievement && achievement.length() > 0) {
                String achStr = achievement.toString().trim();
                if (achStr.length() > 10) {
                    achievements.add(achStr);
                }
            }
        }

        // Deduplicate near-duplicates
        return deduplicate(achievements);
    }

    /**
     * Count percentage-based achievements
     */
    public int countPercentageAchievements(List<String> achievements) {
        int count = 0;
        for (String ach : achievements) {
            if (PERCENTAGE_PATTERN.matcher(ach).find()) count++;
        }
        return count;
    }

    /**
     * Count numerical/scale achievements
     */
    public int countNumericalAchievements(List<String> achievements) {
        int count = 0;
        for (String ach : achievements) {
            if (LARGE_NUMBER_PATTERN.matcher(ach).find() ||
                    NUMBER_WITH_SUFFIX.matcher(ach).find() ||
                    USER_SCALE_PATTERN.matcher(ach).find()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count growth/impact statements
     */
    public int countGrowthStatements(List<String> achievements) {
        int count = 0;
        for (String ach : achievements) {
            String lower = ach.toLowerCase();
            boolean hasVerb = GROWTH_VERBS.stream().anyMatch(lower::contains);
            boolean hasNumber = Pattern.compile("\\d+").matcher(ach).find();
            if (hasVerb && hasNumber) count++;
        }
        return count;
    }

    /**
     * Check for awards/recognition mentions
     */
    public boolean hasAwards(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("award") || lower.contains("honor") ||
               lower.contains("recognition") || lower.contains("scholarship") ||
               lower.contains("employee of the") || lower.contains("top performer") ||
               lower.contains("hall of fame") || lower.contains("rockstar") ||
               lower.contains("star performer");
    }

    /**
     * Check for open source contributions
     */
    public boolean hasOpenSourceContributions(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return (lower.contains("open source") || lower.contains("github"))
               && (lower.contains("contribution") || lower.contains("contributor") ||
                   lower.contains("stars") || lower.contains("forks") ||
                   lower.contains("maintainer") || lower.contains("pull request"));
    }

    private List<String> deduplicate(List<String> items) {
        List<String> unique = new ArrayList<>();
        for (String item : items) {
            boolean isDuplicate = false;
            for (String existing : unique) {
                if (similarity(item, existing) > 0.8) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) unique.add(item);
        }
        return unique;
    }

    /**
     * Simple Jaccard-like similarity for deduplication
     */
    private double similarity(String a, String b) {
        Set<String> wordsA = new HashSet<>(Arrays.asList(a.toLowerCase().split("\\s+")));
        Set<String> wordsB = new HashSet<>(Arrays.asList(b.toLowerCase().split("\\s+")));
        Set<String> intersection = new HashSet<>(wordsA);
        intersection.retainAll(wordsB);
        Set<String> union = new HashSet<>(wordsA);
        union.addAll(wordsB);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    public Set<String> getGrowthVerbs() { return Collections.unmodifiableSet(GROWTH_VERBS); }
}
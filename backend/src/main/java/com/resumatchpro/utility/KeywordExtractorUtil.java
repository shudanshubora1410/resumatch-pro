package com.resumatchpro.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
public class KeywordExtractorUtil {

    private static final Logger log = LoggerFactory.getLogger(KeywordExtractorUtil.class);
    private final Set<String> masterSkills = new HashSet<>();
    private final Map<String, List<String>> skillsByCategory = new LinkedHashMap<>();

    public KeywordExtractorUtil() {
        loadMasterSkills();
    }

    private void loadMasterSkills() {
        try {
            ClassPathResource resource = new ClassPathResource("data/master-skills-list.json");
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream is = resource.getInputStream()) {
                Map<String, List<String>> loaded = mapper.readValue(is,
                        new TypeReference<Map<String, List<String>>>() {});
                skillsByCategory.putAll(loaded);
                for (List<String> skills : loaded.values()) {
                    masterSkills.addAll(skills);
                }
            }
            log.info("Loaded {} skills across {} categories", masterSkills.size(), skillsByCategory.size());
        } catch (Exception e) {
            log.warn("Could not load master skills, using fallback: {}", e.getMessage());
            loadFallbackSkills();
        }
    }

    private void loadFallbackSkills() {
        List<String> fallback = Arrays.asList(
            "java", "python", "javascript", "typescript", "c++", "c#", "ruby", "go",
            "react", "angular", "vue", "spring boot", "django", "flask", "nodejs",
            "mysql", "postgresql", "mongodb", "redis", "docker", "kubernetes",
            "aws", "azure", "gcp", "git", "rest api", "graphql", "machine learning",
            "tensorflow", "pytorch", "html", "css", "sql", "linux", "agile"
        );
        masterSkills.addAll(fallback);
        skillsByCategory.put("common", fallback);
    }

    /**
     * Extract technical skills from text by matching against master skill list.
     * Uses case-insensitive matching with word boundary awareness.
     */
    public Set<String> extractSkills(String text) {
        Set<String> found = new LinkedHashSet<>();
        if (text == null || text.isBlank()) return found;

        String lower = text.toLowerCase();

        // First: exact matches for multi-word skills (e.g., "spring boot", "machine learning")
        for (String skill : masterSkills) {
            if (skill.contains(" ")) {
                if (lower.contains(skill.toLowerCase())) {
                    found.add(skill);
                }
            }
        }

        // Then: single-word matches with word boundary awareness
        String[] words = lower.split("[\\s,;|/()\\[\\]{}]+");
        Set<String> singleWordSkills = new HashSet<>();
        for (String skill : masterSkills) {
            if (!skill.contains(" ")) {
                singleWordSkills.add(skill.toLowerCase());
            }
        }

        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z0-9+#.]", "").trim();
            if (word.length() > 1 && singleWordSkills.contains(word)) {
                found.add(word);
            }
        }

        return found;
    }

    /**
     * Extract skills and return them categorized by domain
     */
    public Map<String, List<String>> extractSkillsByCategory(String text) {
        Map<String, List<String>> categorized = new LinkedHashMap<>();
        Set<String> allFound = extractSkills(text);

        for (Map.Entry<String, List<String>> category : skillsByCategory.entrySet()) {
            List<String> matched = new ArrayList<>();
            for (String skill : category.getValue()) {
                if (allFound.contains(skill)) {
                    matched.add(skill);
                }
            }
            if (!matched.isEmpty()) {
                categorized.put(category.getKey(), matched);
            }
        }
        return categorized;
    }

    /**
     * Check if a specific keyword/skill exists in the text
     */
    public boolean containsKeyword(String text, String keyword) {
        if (text == null || keyword == null) return false;
        return text.toLowerCase().contains(keyword.toLowerCase().trim());
    }

    /**
     * Find which keywords from a list are present in text.
     * Returns matched keywords.
     */
    public Set<String> findMatchedKeywords(String text, List<String> keywords) {
        Set<String> matched = new LinkedHashSet<>();
        if (text == null || keywords == null) return matched;
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (kw != null && lower.contains(kw.toLowerCase().trim())) {
                matched.add(kw);
            }
        }
        return matched;
    }

    /**
     * Find which keywords from a list are MISSING from text
     */
    public Set<String> findMissingKeywords(String text, List<String> keywords) {
        Set<String> missing = new LinkedHashSet<>();
        if (keywords == null) return missing;
        Set<String> matched = findMatchedKeywords(text, keywords);
        for (String kw : keywords) {
            if (kw != null && !matched.contains(kw)) {
                missing.add(kw);
            }
        }
        return missing;
    }

    /**
     * Determine which section of the resume a keyword appears in.
     * Returns the section weight for confidence scoring.
     */
    public double getSectionWeight(String keyword, Map<String, String> sections) {
        String lowerKeyword = keyword.toLowerCase();

        // Check in priority order
        for (Map.Entry<String, String> section : sections.entrySet()) {
            if (section.getValue() != null &&
                    section.getValue().toLowerCase().contains(lowerKeyword)) {
                return switch (section.getKey()) {
                    case "SKILLS" -> 1.0;
                    case "EXPERIENCE" -> 0.85;
                    case "PROJECTS" -> 0.75;
                    case "SUMMARY" -> 0.65;
                    default -> 0.5;
                };
            }
        }
        return 0.5; // Default weight for keyword found somewhere in the text
    }

    /**
     * Count frequency of a keyword in text
     */
    public int countFrequency(String text, String keyword) {
        if (text == null || keyword == null) return 0;
        String lower = text.toLowerCase();
        String lowerKw = keyword.toLowerCase();
        int count = 0;
        int idx = 0;
        while ((idx = lower.indexOf(lowerKw, idx)) != -1) {
            count++;
            idx += lowerKw.length();
        }
        return count;
    }

    /**
     * Get frequency weight: how heavily to weigh keyword based on occurrence count
     */
    public double getFrequencyWeight(int count) {
        if (count >= 3) return 1.0;
        if (count >= 2) return 0.85;
        if (count >= 1) return 0.7;
        return 0.0;
    }

    public Set<String> getMasterSkills() {
        return Collections.unmodifiableSet(masterSkills);
    }

    public Map<String, List<String>> getSkillsByCategory() {
        return Collections.unmodifiableMap(skillsByCategory);
    }
}
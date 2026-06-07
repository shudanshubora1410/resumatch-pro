package com.resumatchpro.utility;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.*;

@Component
public class SectionDetectorUtil {

    // Patterns to detect resume section headers
    private static final Map<String, List<String>> SECTION_PATTERNS = new LinkedHashMap<>();

    static {
        SECTION_PATTERNS.put("SKILLS", Arrays.asList(
            "skills", "technical skills", "core competencies", "technologies",
            "technology stack", "tech stack", "languages", "frameworks",
            "tools & technologies", "tools and technologies", "programming",
            "technical proficiency", "areas of expertise", "expertise"
        ));
        SECTION_PATTERNS.put("EXPERIENCE", Arrays.asList(
            "experience", "work experience", "employment", "work history",
            "professional experience", "professional background",
            "career history", "positions held", "employment history",
            "relevant experience", "industry experience"
        ));
        SECTION_PATTERNS.put("EDUCATION", Arrays.asList(
            "education", "academic", "qualification", "degree",
            "educational background", "academic background",
            "academic qualification", "educational qualification",
            "studies", "schooling"
        ));
        SECTION_PATTERNS.put("PROJECTS", Arrays.asList(
            "projects", "personal projects", "academic projects",
            "key projects", "notable projects", "side projects",
            "open source", "portfolio", "project experience",
            "major projects", "project work"
        ));
        SECTION_PATTERNS.put("CERTIFICATIONS", Arrays.asList(
            "certifications", "certificates", "courses", "credentials",
            "licenses", "training", "professional development",
            "online courses", "moocs", "workshops"
        ));
        SECTION_PATTERNS.put("SUMMARY", Arrays.asList(
            "summary", "objective", "profile", "about", "overview",
            "career objective", "professional summary", "about me",
            "personal statement", "professional profile", "introduction",
            "executive summary"
        ));
        SECTION_PATTERNS.put("ACHIEVEMENTS", Arrays.asList(
            "achievements", "accomplishments", "awards", "honors",
            "recognition", "key achievements", "notable achievements",
            "highlights", "milestones"
        ));
    }

    /**
     * Detect sections in resume text and return map of section name → content.
     * Uses header-matching: looks for lines that match section headers,
     * then groups content until next header.
     */
    public Map<String, String> detectSections(String resumeText) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (resumeText == null || resumeText.isBlank()) return sections;

        String[] lines = resumeText.split("\\n");
        String currentSection = "HEADER"; // content before first section
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            String cleaned = line.trim().toLowerCase();

            // Remove trailing colons and common formatting
            cleaned = cleaned.replaceAll("[:\\-•*]+$", "").trim();

            String matchedSection = matchSectionHeader(cleaned);

            if (matchedSection != null && cleaned.length() < 60) {
                // Save previous section
                if (currentContent.length() > 0 && currentSection != null) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                currentSection = matchedSection;
                currentContent = new StringBuilder();
            } else {
                if (currentContent.length() > 0) currentContent.append("\n");
                currentContent.append(line);
            }
        }

        // Save last section
        if (currentContent.length() > 0 && currentSection != null) {
            sections.put(currentSection, currentContent.toString().trim());
        }

        return sections;
    }

    /**
     * Try to match a line as a section header.
     * Returns the section name (e.g., "SKILLS") or null.
     */
    private String matchSectionHeader(String line) {
        for (Map.Entry<String, List<String>> entry : SECTION_PATTERNS.entrySet()) {
            for (String pattern : entry.getValue()) {
                // Exact match, or line starts with pattern, or line equals pattern
                if (line.equals(pattern) || line.startsWith(pattern + " ")
                        || line.equals(pattern + ":")) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Get the set of all detected section names
     */
    public Set<String> getDetectedSectionNames(Map<String, String> sections) {
        return sections.keySet();
    }

    /**
     * Get list of expected sections that are missing
     */
    public List<String> getMissingSections(Map<String, String> sections) {
        List<String> missing = new ArrayList<>();
        // Critical sections
        if (!sections.containsKey("SKILLS")) missing.add("Skills");
        if (!sections.containsKey("EXPERIENCE")) missing.add("Experience");
        if (!sections.containsKey("EDUCATION")) missing.add("Education");
        // Nice-to-have sections
        if (!sections.containsKey("PROJECTS")) missing.add("Projects");
        if (!sections.containsKey("SUMMARY")) missing.add("Summary/Objective");
        // Bonus sections
        if (!sections.containsKey("CERTIFICATIONS")) missing.add("Certifications");
        if (!sections.containsKey("ACHIEVEMENTS")) missing.add("Achievements");
        return missing;
    }

    /**
     * Extract bullet points from experience/project sections
     */
    public List<String> extractBulletPoints(String sectionContent) {
        List<String> bullets = new ArrayList<>();
        if (sectionContent == null) return bullets;

        String[] lines = sectionContent.split("\\n");
        for (String line : lines) {
            String cleaned = line.trim();
            // Skip empty lines and section headers
            if (cleaned.isEmpty() || cleaned.length() < 10) continue;

            // Detect bullet markers
            if (cleaned.startsWith("-") || cleaned.startsWith("•") ||
                    cleaned.startsWith("*") || cleaned.startsWith("–") ||
                    cleaned.matches("^\\d+[\\.\\)]\\s.*")) {
                bullets.add(cleaned.replaceFirst("^[-•*–]+\\s*", "")
                                   .replaceFirst("^\\d+[\\.\\)]\\s*", "")
                                   .trim());
            } else {
                bullets.add(cleaned);
            }
        }
        return bullets;
    }

    public Map<String, List<String>> getSectionPatterns() {
        return Collections.unmodifiableMap(SECTION_PATTERNS);
    }
}
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
public class SkillSynonymUtil {

    private static final Logger log = LoggerFactory.getLogger(SkillSynonymUtil.class);
    private final Map<String, String> synonymMap = new HashMap<>();
    private final Set<String> allKnownTerms = new HashSet<>();

    public SkillSynonymUtil() {
        loadSynonyms();
    }

    private void loadSynonyms() {
        try {
            ClassPathResource resource = new ClassPathResource("data/skill-synonyms.json");
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream is = resource.getInputStream()) {
                Map<String, String> loaded = mapper.readValue(is,
                        new TypeReference<Map<String, String>>() {});
                synonymMap.putAll(loaded);
                // Build reverse lookup: all known short forms and full forms
                allKnownTerms.addAll(loaded.keySet());
                allKnownTerms.addAll(loaded.values());
            }
            log.info("Loaded {} skill synonyms", synonymMap.size());
        } catch (Exception e) {
            log.warn("Could not load synonyms file, using fallback: {}", e.getMessage());
            loadFallbackSynonyms();
        }
    }

    private void loadFallbackSynonyms() {
        Map<String, String> fallback = Map.ofEntries(
            Map.entry("js", "javascript"),
            Map.entry("reactjs", "react"),
            Map.entry("node", "nodejs"),
            Map.entry("ml", "machine learning"),
            Map.entry("ai", "artificial intelligence"),
            Map.entry("dl", "deep learning"),
            Map.entry("k8s", "kubernetes"),
            Map.entry("mongo", "mongodb"),
            Map.entry("postgres", "postgresql"),
            Map.entry("aws", "amazon web services"),
            Map.entry("gcp", "google cloud platform"),
            Map.entry("tf", "tensorflow"),
            Map.entry("cv", "computer vision"),
            Map.entry("nlp", "natural language processing"),
            Map.entry("oop", "object oriented programming"),
            Map.entry("dsa", "data structures and algorithms"),
            Map.entry("ts", "typescript"),
            Map.entry("py", "python"),
            Map.entry("go", "golang"),
            Map.entry("cpp", "c++"),
            Map.entry("csharp", "c#"),
            Map.entry("mssql", "sql server"),
            Map.entry("cicd", "ci/cd")
        );
        synonymMap.putAll(fallback);
        allKnownTerms.addAll(fallback.keySet());
        allKnownTerms.addAll(fallback.values());
    }

    /**
     * Normalize a skill/term to its canonical form.
     * Returns the original term if no synonym mapping exists.
     */
    public String normalize(String term) {
        if (term == null) return null;
        String lower = term.toLowerCase().trim();

        // Direct match
        if (synonymMap.containsKey(lower)) {
            return synonymMap.get(lower);
        }

        // Check if it's already the canonical form (a value in the map)
        if (allKnownTerms.contains(lower)) {
            return lower;
        }

        // Partial match: check if term contains a known abbreviation
        for (Map.Entry<String, String> entry : synonymMap.entrySet()) {
            if (lower.equals(entry.getKey())) {
                return entry.getValue();
            }
        }

        return lower;
    }

    /**
     * Normalize a list of terms
     */
    public List<String> normalizeAll(List<String> terms) {
        List<String> normalized = new ArrayList<>();
        for (String term : terms) {
            String norm = normalize(term);
            if (norm != null && !normalized.contains(norm)) {
                normalized.add(norm);
            }
        }
        return normalized;
    }

    /**
     * Check if term is a recognized technical skill
     */
    public boolean isKnownTerm(String term) {
        return term != null && allKnownTerms.contains(term.toLowerCase().trim());
    }

    public Map<String, String> getSynonymMap() {
        return Collections.unmodifiableMap(synonymMap);
    }
}
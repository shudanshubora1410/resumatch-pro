package com.resumatchpro.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class StopwordsUtil {

    private static final Logger log = LoggerFactory.getLogger(StopwordsUtil.class);
    private final Set<String> stopwords = new HashSet<>();

    public StopwordsUtil() {
        loadStopwords();
    }

    private void loadStopwords() {
        try {
            ClassPathResource resource = new ClassPathResource("data/stopwords.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] words = line.trim().toLowerCase().split("\\s+");
                    for (String word : words) {
                        if (!word.isBlank()) {
                            stopwords.add(word);
                        }
                    }
                }
            }
            log.info("Loaded {} stopwords", stopwords.size());
        } catch (Exception e) {
            log.warn("Could not load stopwords file, using fallback: {}", e.getMessage());
            loadFallbackStopwords();
        }
    }

    private void loadFallbackStopwords() {
        String[] fallback = {
            "the", "and", "is", "a", "an", "in", "of", "to", "for", "with",
            "on", "at", "by", "as", "it", "its", "was", "are", "been", "have",
            "has", "had", "will", "would", "could", "should", "may", "might",
            "this", "that", "these", "those", "from", "into", "through",
            "during", "before", "after", "above", "below", "between", "out",
            "some", "each", "which", "their", "them", "such", "when", "if",
            "what", "who", "how", "not", "but", "we", "he", "she", "they",
            "his", "her", "or", "than", "then", "also", "just", "about",
            "over", "can", "only", "most", "other", "new", "more", "very",
            "up", "like", "being", "well", "because", "all", "into", "make",
            "no", "much", "any", "same", "see", "get", "back", "still",
            "too", "here", "there", "where", "now", "so", "do", "does",
            "did", "done", "doing", "my", "your", "our", "me", "us", "i"
        };
        stopwords.addAll(Arrays.asList(fallback));
        log.info("Loaded {} fallback stopwords", stopwords.size());
    }

    public boolean isStopword(String word) {
        return word != null && stopwords.contains(word.toLowerCase().trim());
    }

    public List<String> removeStopwords(List<String> words) {
        List<String> filtered = new ArrayList<>();
        for (String word : words) {
            if (word == null || word.length() < 2) continue;
            if (!isStopword(word)) {
                filtered.add(word);
            }
        }
        return filtered;
    }

    public Set<String> getStopwords() {
        return Collections.unmodifiableSet(stopwords);
    }
}
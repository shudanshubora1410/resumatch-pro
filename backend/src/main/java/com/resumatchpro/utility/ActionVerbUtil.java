package com.resumatchpro.utility;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ActionVerbUtil {

    // Strong action verbs → signal impact and ownership
    private static final Set<String> STRONG_VERBS = new HashSet<>(Arrays.asList(
        "architected", "spearheaded", "pioneered", "optimized", "engineered",
        "orchestrated", "deployed", "automated", "scaled", "delivered",
        "launched", "built", "designed", "developed", "implemented",
        "established", "transformed", "accelerated", "streamlined",
        "revamped", "consolidated", "instituted", "devised", "formulated",
        "directed", "chaired", "mentored", "negotiated", "secured",
        "generated", "achieved", "exceeded", "surpassed", "maximized",
        "minimized", "eliminated", "resolved", "restructured", "integrated",
        "migrated", "standardized", "centralized", "decentralized",
        "modernized", "revitalized", "reengineered", "rearchitected",
        "containerized", "microserviced", "decoupled", "abstracted",
        "refactored", "open-sourced", "contributed", "authored"
    ));

    // Weak phrases → penalize (passive, vague, responsibility-claiming)
    private static final Set<String> WEAK_PHRASES = new HashSet<>(Arrays.asList(
        "worked on", "involved in", "helped with", "assisted",
        "participated in", "was responsible for", "helped",
        "assisted in", "contributed to", "part of team",
        "member of", "duties included", "responsibilities included",
        "tasked with", "given the role", "assigned to",
        "worked with", "supported", "aided", "backed up"
    ));

    // Leadership / team signal words
    private static final Set<String> LEADERSHIP_TERMS = new HashSet<>(Arrays.asList(
        "led", "managed", "supervised", "coordinated", "headed",
        "directed", "oversaw", "governed", "guided", "coached",
        "mentored", "trained", "onboarded", "facilitated",
        "stakeholder", "cross-functional", "multi-team", "enterprise"
    ));

    // Architecture / system design signal words
    private static final Set<String> ARCHITECTURE_TERMS = new HashSet<>(Arrays.asList(
        "architecture", "system design", "high-level design", "low-level design",
        "scalability", "fault tolerance", "high availability", "distributed",
        "microservices", "monolith", "event-driven", "message queue",
        "load balancer", "caching strategy", "database design",
        "api gateway", "service mesh", "circuit breaker", "saga pattern",
        "cqrs", "event sourcing", "domain-driven design", "ddd",
        "hexagonal architecture", "clean architecture", "onion architecture"
    ));

    /**
     * Check if a word is a strong action verb
     */
    public boolean isStrongVerb(String word) {
        return word != null && STRONG_VERBS.contains(word.toLowerCase().trim());
    }

    /**
     * Check if text contains weak phrasing
     */
    public boolean containsWeakPhrase(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String weak : WEAK_PHRASES) {
            if (lower.contains(weak)) return true;
        }
        return false;
    }

    /**
     * Find all weak phrases in text
     */
    public List<String> findWeakPhrases(String text) {
        List<String> found = new ArrayList<>();
        if (text == null) return found;
        String lower = text.toLowerCase();
        for (String weak : WEAK_PHRASES) {
            if (lower.contains(weak)) {
                found.add(weak);
            }
        }
        return found;
    }

    /**
     * Count strong action verbs in a list of bullet points
     */
    public int countStrongVerbs(List<String> bulletPoints) {
        int count = 0;
        for (String bullet : bulletPoints) {
            String[] words = bullet.toLowerCase().split("\\s+");
            if (words.length > 0 && STRONG_VERBS.contains(words[0])) {
                count++;
            }
            // Also check anywhere in the bullet for additional strong verbs
            for (String word : words) {
                if (STRONG_VERBS.contains(word)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Count weak phrases used in experience descriptions
     */
    public int countWeakPhrases(List<String> bulletPoints) {
        int count = 0;
        for (String bullet : bulletPoints) {
            count += findWeakPhrases(bullet).size();
        }
        return count;
    }

    /**
     * Check for leadership terms in text
     */
    public boolean hasLeadershipTerms(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return LEADERSHIP_TERMS.stream().anyMatch(lower::contains);
    }

    /**
     * Check for architecture/system design terms
     */
    public boolean hasArchitectureTerms(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return ARCHITECTURE_TERMS.stream().anyMatch(lower::contains);
    }

    public Set<String> getStrongVerbs() { return Collections.unmodifiableSet(STRONG_VERBS); }
    public Set<String> getWeakPhrases() { return Collections.unmodifiableSet(WEAK_PHRASES); }
    public Set<String> getLeadershipTerms() { return Collections.unmodifiableSet(LEADERSHIP_TERMS); }
    public Set<String> getArchitectureTerms() { return Collections.unmodifiableSet(ARCHITECTURE_TERMS); }
}
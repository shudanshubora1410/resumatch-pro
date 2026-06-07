package com.resumatchpro.utility;

import com.resumatchpro.model.JobListing;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RecommendationEngineUtil {

    private final KeywordExtractorUtil keywordExtractor;
    private final SkillSynonymUtil skillSynonym;

    public RecommendationEngineUtil(KeywordExtractorUtil keywordExtractor,
                                     SkillSynonymUtil skillSynonym) {
        this.keywordExtractor = keywordExtractor;
        this.skillSynonym = skillSynonym;
    }

    /**
     * Rank jobs by skill overlap with a set of extracted skills.
     * Returns top N matches with detailed overlap info.
     */
    public List<JobMatch> rankJobs(Set<String> candidateSkills, List<JobListing> jobs, int maxResults) {
        List<JobMatch> matches = new ArrayList<>();

        for (JobListing job : jobs) {
            List<String> jobSkills = parseSkills(job.getRequiredSkills());
            if (jobSkills.isEmpty()) continue;

            Set<String> matched = new LinkedHashSet<>();
            Set<String> missing = new LinkedHashSet<>();

            for (String skill : jobSkills) {
                String cleanSkill = skill.toLowerCase().trim();
                if (candidateSkills.contains(cleanSkill)
                        || candidateSkills.stream().anyMatch(cs -> cs.contains(cleanSkill)
                        || cleanSkill.contains(cs))) {
                    matched.add(skill);
                } else {
                    missing.add(skill);
                }
            }

            double matchPct = jobSkills.size() > 0
                    ? ((double) matched.size() / jobSkills.size()) * 100
                    : 0;

            matches.add(new JobMatch(job, matchPct, matched, missing));
        }

        // Sort by match percentage descending
        matches.sort((a, b) -> Double.compare(b.matchPercentage, a.matchPercentage));

        return matches.subList(0, Math.min(maxResults, matches.size()));
    }

    private List<String> parseSkills(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Result: a job match with percentage and details
     */
    public static class JobMatch {
        public final JobListing job;
        public final double matchPercentage;
        public final Set<String> matchedSkills;
        public final Set<String> missingSkills;

        public JobMatch(JobListing job, double matchPercentage,
                        Set<String> matchedSkills, Set<String> missingSkills) {
            this.job = job;
            this.matchPercentage = matchPercentage;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
        }
    }
}

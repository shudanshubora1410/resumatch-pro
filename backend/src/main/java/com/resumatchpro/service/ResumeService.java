package com.resumatchpro.service;

import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.utility.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);
    private static final int MAX_UPLOADS_PER_HOUR = 10;
    private static final long MAX_FILE_SIZE_KB = 5120; // 5MB

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final FileParserUtil fileParserUtil;
    private final NLPProcessorUtil nlpProcessor;
    private final FileStorageUtil fileStorageUtil;

    @Transactional
    public Resume uploadResume(Long userId, MultipartFile file) throws InvalidFileException {
        // Rate limit check
        long recentUploads = resumeRepository.countByUserIdAndIsActiveTrue(userId);
        if (recentUploads >= MAX_UPLOADS_PER_HOUR) {
            throw new RateLimitExceededException(
                "Upload limit reached. Max " + MAX_UPLOADS_PER_HOUR + " resumes per hour.", 3600);
        }

        // Parse file
        FileParserUtil.ParsedResult parsed = fileParserUtil.parse(file);
        String storedFilename;

        try {
            storedFilename = fileStorageUtil.store(file.getInputStream(),
                    parsed.getOriginalFilename(), parsed.getFileType());
        } catch (IOException e) {
            throw new InvalidFileException("Failed to store file: " + e.getMessage());
        }

        // Run NLP
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(parsed.getExtractedText());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Resume resume = Resume.builder()
                .user(user)
                .originalFilename(parsed.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(storagePath(storedFilename))
                .fileType(parsed.getFileType())
                .fileSizeKb(file.getSize() / 1024)
                .extractedText(parsed.getExtractedText())
                .wordCount(parsed.getWordCount())
                .isActive(true)
                .build();

        resume = resumeRepository.save(resume);
        log.info("Resume uploaded: userId={} filename={} words={}",
                userId, storedFilename, parsed.getWordCount());

        return resume;
    }

    public Page<Resume> getUserResumes(Long userId, Pageable pageable) {
        return resumeRepository.findActiveByUserId(userId, pageable);
    }

    public List<Resume> getAllUserResumes(Long userId) {
        return resumeRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public Resume getResume(Long resumeId, Long userId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));
    }

    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", resumeId));
        resume.softDelete();
        fileStorageUtil.delete(resume.getStoredFilename());
        resumeRepository.save(resume);
    }

    /**
     * Standalone quality check - score resume format/structure without comparing to any job
     */
    public Map<String, Object> qualityCheck(Long resumeId, Long userId) {
        Resume resume = getResume(resumeId, userId);
        NLPProcessorUtil.ProcessedResume processed = nlpProcessor.process(resume.getExtractedText());

        Map<String, Object> quality = new LinkedHashMap<>();
        quality.put("resumeId", resumeId);
        quality.put("wordCount", processed.getWordCount());
        quality.put("detectedSections", processed.getDetectedSections());
        quality.put("missingSections", processed.getMissingSections());
        quality.put("extractedSkills", processed.getExtractedSkills());
        quality.put("skillCount", processed.getExtractedSkills().size());
        quality.put("contactInfo", processed.getContactInfo());
        quality.put("atsFriendly", processed.isAtsFriendly());
        quality.put("hasAwards", processed.isHasAwards());
        quality.put("estimatedExperienceYears", processed.getEstimatedExperienceYears());

        // Quality tier
        int sectionCount = processed.getDetectedSections().size();
        int skillCount = processed.getExtractedSkills().size();
        int achievementCount = processed.getAchievements().size();
        long contactCount = processed.getContactInfo().values().stream()
                .filter(Boolean.TRUE::equals).count();

        if (sectionCount >= 5 && skillCount >= 15 && achievementCount >= 3 && contactCount >= 3) {
            quality.put("qualityTier", "EXCELLENT");
        } else if (sectionCount >= 4 && skillCount >= 10 && achievementCount >= 1 && contactCount >= 2) {
            quality.put("qualityTier", "GOOD");
        } else if (sectionCount >= 3 && skillCount >= 5) {
            quality.put("qualityTier", "FAIR");
        } else {
            quality.put("qualityTier", "NEEDS_IMPROVEMENT");
        }

        return quality;
    }

    /**
     * Compare two resume versions
     */
    public Map<String, Object> compareResumes(Long resumeId1, Long resumeId2, Long userId) {
        Resume r1 = getResume(resumeId1, userId);
        Resume r2 = getResume(resumeId2, userId);

        NLPProcessorUtil.ProcessedResume p1 = nlpProcessor.process(r1.getExtractedText());
        NLPProcessorUtil.ProcessedResume p2 = nlpProcessor.process(r2.getExtractedText());

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("resume1", Map.of("id", resumeId1, "uploadDate", r1.getUploadDate(),
                "wordCount", p1.getWordCount(), "skills", p1.getExtractedSkills(),
                "sectionCount", p1.getDetectedSections().size()));
        comparison.put("resume2", Map.of("id", resumeId2, "uploadDate", r2.getUploadDate(),
                "wordCount", p2.getWordCount(), "skills", p2.getExtractedSkills(),
                "sectionCount", p2.getDetectedSections().size()));

        // Skills gained
        Set<String> newSkills = new HashSet<>(p2.getExtractedSkills());
        newSkills.removeAll(p1.getExtractedSkills());
        comparison.put("skillsGained", newSkills);

        // Sections added
        Set<String> newSections = new HashSet<>(p2.getDetectedSections());
        newSections.removeAll(p1.getDetectedSections());
        comparison.put("sectionsAdded", newSections);

        return comparison;
    }

    private String storagePath(String filename) {
        return "./uploads/resumes/" + filename;
    }
}

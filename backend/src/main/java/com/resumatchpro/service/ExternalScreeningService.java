package com.resumatchpro.service;

import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.utility.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExternalScreeningService {

    private static final Logger log = LoggerFactory.getLogger(ExternalScreeningService.class);

    private final ExternalApplicationRepository externalAppRepository;
    private final JobListingRepository jobListingRepository;
    private final UserRepository userRepository;
    private final FileParserUtil fileParserUtil;
    private final FileStorageUtil fileStorageUtil;

    @Async("analysisExecutor")
    @Transactional
    public CompletableFuture<List<ExternalApplication>> bulkScreen(
            Long jobId, Long recruiterId, List<MultipartFile> files) {

        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", recruiterId));

        List<ExternalApplication> results = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileParserUtil.ParsedResult parsed = fileParserUtil.parse(file);
                String storedFilename = fileStorageUtil.store(
                        file.getInputStream(), parsed.getOriginalFilename(), parsed.getFileType());

                ExternalApplication extApp = ExternalApplication.builder()
                        .jobListing(job)
                        .recruiter(recruiter)
                        .resumeFilePath(storedFilename)
                        .originalFilename(parsed.getOriginalFilename())
                        .candidateNameExtracted("External Candidate")
                        .candidateEmailExtracted("N/A")
                        .build();

                extApp = externalAppRepository.save(extApp);
                results.add(extApp);

            } catch (Exception e) {
                log.warn("Failed to process external file {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }

        log.info("Bulk screen complete: jobId={} processed={}/{}", jobId, results.size(), files.size());
        return CompletableFuture.completedFuture(results);
    }

    public List<ExternalApplication> getResults(Long jobId) {
        return externalAppRepository.findByJobListingId(jobId);
    }
}

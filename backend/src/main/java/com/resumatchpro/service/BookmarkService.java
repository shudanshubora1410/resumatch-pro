package com.resumatchpro.service;

import com.resumatchpro.exception.DuplicateApplicationException;
import com.resumatchpro.exception.ResourceNotFoundException;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final JobBookmarkRepository bookmarkRepository;
    private final JobListingRepository jobListingRepository;
    private final UserRepository userRepository;

    @Transactional
    public void bookmark(Long userId, Long jobId) {
        if (bookmarkRepository.existsByUserIdAndJobListingId(userId, jobId)) {
            throw new DuplicateApplicationException("Job already bookmarked");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        JobListing job = jobListingRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        JobBookmark bookmark = JobBookmark.builder().user(user).jobListing(job).build();
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Long userId, Long jobId) {
        bookmarkRepository.findByUserIdAndJobListingId(userId, jobId)
                .ifPresent(bookmarkRepository::delete);
    }

    public Page<JobBookmark> getBookmarks(Long userId, Pageable pageable) {
        return bookmarkRepository.findByUserIdOrderBySavedAtDesc(userId, pageable);
    }

    public boolean isBookmarked(Long userId, Long jobId) {
        return bookmarkRepository.existsByUserIdAndJobListingId(userId, jobId);
    }
}

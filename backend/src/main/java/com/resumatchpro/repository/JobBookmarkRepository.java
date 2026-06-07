package com.resumatchpro.repository;

import com.resumatchpro.model.JobBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobBookmarkRepository extends JpaRepository<JobBookmark, Long> {

    Page<JobBookmark> findByUserIdOrderBySavedAtDesc(Long userId, Pageable pageable);

    Optional<JobBookmark> findByUserIdAndJobListingId(Long userId, Long jobId);

    boolean existsByUserIdAndJobListingId(Long userId, Long jobId);

    @Query("SELECT b FROM JobBookmark b WHERE b.jobListing.id = :jobId")
    List<JobBookmark> findByJobListingId(@Param("jobId") Long jobId);
}

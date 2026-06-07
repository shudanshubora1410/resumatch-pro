package com.resumatchpro.repository;

import com.resumatchpro.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByJobSeekerIdAndJobListingId(Long seekerId, Long jobId);

    boolean existsByJobSeekerIdAndJobListingId(Long seekerId, Long jobId);

    Page<Application> findByJobSeekerId(Long seekerId, Pageable pageable);

    List<Application> findByJobListingId(Long jobId);

    Page<Application> findByJobListingId(Long jobId, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobListing.id = :jobId ORDER BY a.id")
    List<Application> findAllByJobListingId(@Param("jobId") Long jobId);

    long countByJobListingIdAndStatus(Long jobId, Application.ApplicationStatus status);

    long countByJobSeekerId(Long seekerId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobListing.recruiter.id = :recruiterId")
    long countByRecruiterId(@Param("recruiterId") Long recruiterId);

    @Query("SELECT a FROM Application a WHERE a.appliedAt BETWEEN :start AND :end")
    List<Application> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

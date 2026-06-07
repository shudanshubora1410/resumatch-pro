package com.resumatchpro.repository;

import com.resumatchpro.model.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    Optional<ResumeAnalysis> findByApplicationId(Long applicationId);

    List<ResumeAnalysis> findByJobListingId(Long jobId);

    @Query("SELECT ra FROM ResumeAnalysis ra WHERE ra.jobListing.id = :jobId ORDER BY ra.finalScore DESC")
    List<ResumeAnalysis> findByJobIdOrderByScore(@Param("jobId") Long jobId);

    @Query("SELECT AVG(ra.finalScore) FROM ResumeAnalysis ra WHERE ra.analysisStatus = 'COMPLETED'")
    Double getPlatformAverageScore();
}

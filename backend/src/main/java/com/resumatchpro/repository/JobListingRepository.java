package com.resumatchpro.repository;

import com.resumatchpro.model.JobListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobListingRepository extends JpaRepository<JobListing, Long>,
        JpaSpecificationExecutor<JobListing> {

    Page<JobListing> findByRecruiterIdAndDeletedAtIsNull(Long recruiterId, Pageable pageable);

    @Query("SELECT j FROM JobListing j WHERE j.status = 'ACTIVE' AND j.deletedAt IS NULL " +
           "AND (j.applicationDeadline IS NULL OR j.applicationDeadline >= CURRENT_DATE)")
    Page<JobListing> findActiveJobs(Pageable pageable);

    @Query("SELECT j FROM JobListing j WHERE j.status = 'ACTIVE' AND j.deletedAt IS NULL")
    List<JobListing> findAllActiveJobs();

    List<JobListing> findByRecruiterId(Long recruiterId);

    @Query("SELECT COUNT(j) FROM JobListing j WHERE j.recruiter.id = :recruiterId AND j.deletedAt IS NULL")
    long countByRecruiter(@Param("recruiterId") Long recruiterId);

    @Query("SELECT COUNT(j) FROM JobListing j WHERE j.status = 'ACTIVE' AND j.deletedAt IS NULL")
    long countActiveJobs();
}

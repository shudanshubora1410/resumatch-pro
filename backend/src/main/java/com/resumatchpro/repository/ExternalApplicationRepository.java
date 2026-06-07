package com.resumatchpro.repository;

import com.resumatchpro.model.ExternalApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExternalApplicationRepository extends JpaRepository<ExternalApplication, Long> {

    List<ExternalApplication> findByJobListingId(Long jobId);
    List<ExternalApplication> findByJobListingIdAndRecruiterId(Long jobId, Long recruiterId);
}

package com.resumatchpro.repository;

import com.resumatchpro.model.InterviewSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {

    Optional<InterviewSchedule> findByApplicationId(Long applicationId);
}

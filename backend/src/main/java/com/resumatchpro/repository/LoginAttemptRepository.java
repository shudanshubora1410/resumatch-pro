package com.resumatchpro.repository;

import com.resumatchpro.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT COUNT(l) FROM LoginAttempt l WHERE l.email = :email " +
           "AND l.isSuccessful = false AND l.attemptTime > :since")
    long countFailedAttemptsSince(@Param("email") String email, @Param("since") LocalDateTime since);
}

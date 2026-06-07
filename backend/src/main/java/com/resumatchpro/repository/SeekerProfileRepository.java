package com.resumatchpro.repository;

import com.resumatchpro.model.SeekerProfile;
import com.resumatchpro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SeekerProfileRepository extends JpaRepository<SeekerProfile, Long> {
    Optional<SeekerProfile> findByUser(User user);
    Optional<SeekerProfile> findByUserId(Long userId);
}

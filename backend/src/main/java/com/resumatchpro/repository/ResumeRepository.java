package com.resumatchpro.repository;

import com.resumatchpro.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserIdAndIsActiveTrue(Long userId);
    Page<Resume> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT r FROM Resume r WHERE r.user.id = :userId AND r.deletedAt IS NULL ORDER BY r.uploadDate DESC")
    Page<Resume> findActiveByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByUserIdAndIsActiveTrue(Long userId);
}

package com.resumatchpro.repository;

import com.resumatchpro.model.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    Page<AdminAuditLog> findByAdminIdOrderByTimestampDesc(Long adminId, Pageable pageable);

    Page<AdminAuditLog> findByActionContaining(String action, Pageable pageable);
}

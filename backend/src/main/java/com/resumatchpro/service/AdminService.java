package com.resumatchpro.service;

import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobListingRepository jobListingRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ScoringService scoringService;

    // ==================== USER MANAGEMENT ====================

    public Page<User> getUsers(String role, Pageable pageable) {
        if (role != null && !role.isBlank()) {
            try {
                User.UserRole r = User.UserRole.valueOf(role.toUpperCase());
                return userRepository.findByRoleAndDeletedAtIsNull(r, pageable);
            } catch (IllegalArgumentException ignored) {}
        }
        return userRepository.findAllActive(pageable);
    }

    @Transactional
    public User updateUserStatus(Long userId, boolean activate) {
        User user = userRepository.findById(userId)
                .orElseThrow();
        user.setIsActive(activate);
        if (!activate) user.softDelete();
        return userRepository.save(user);
    }

    // ==================== ANALYTICS ====================

    public Map<String, Object> getPlatformOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSeekers", userRepository.countByRole(User.UserRole.JOB_SEEKER));
        stats.put("totalRecruiters", userRepository.countByRole(User.UserRole.RECRUITER));
        stats.put("totalAdmins", userRepository.countByRole(User.UserRole.ADMIN));
        stats.put("activeJobs", jobListingRepository.countActiveJobs());
        stats.put("totalResumes", resumeRepository.count());
        stats.put("avgPlatformScore", scoringService.getPlatformAverage());
        return stats;
    }

    // ==================== AUDIT ====================

    @Transactional
    public AdminAuditLog logAction(Long adminId, String action, String targetType,
                                    Long targetId, String details, String ip) {
        User admin = userRepository.findById(adminId).orElseThrow();
        AdminAuditLog log = AdminAuditLog.builder()
                .admin(admin).action(action).targetType(targetType)
                .targetId(targetId).details(details).ipAddress(ip)
                .build();
        return auditLogRepository.save(log);
    }

    public Page<AdminAuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    // ==================== EXPORT ====================

    public String exportUsersCsv() {
        StringBuilder sb = new StringBuilder("ID,Name,Email,Role,Active,Registered\n");
        userRepository.findAll().forEach(u -> sb.append(String.format("%d,%s,%s,%s,%s,%s\n",
                u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getIsActive(), u.getCreatedAt())));
        return sb.toString();
    }
}

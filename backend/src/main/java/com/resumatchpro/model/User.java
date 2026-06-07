package com.resumatchpro.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "full_name", nullable = false, length = 100) private String fullName;
    @Column(nullable = false, unique = true, length = 100) private String email;
    @Column(nullable = false, length = 255) private String password;
    @Column(length = 15) private String phone;
    @Column(length = 100) private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserRole role = UserRole.JOB_SEEKER;
    @Column(name = "is_active") private Boolean isActive = true;
    @Column(name = "is_email_verified") private Boolean isEmailVerified = false;
    @Column(name = "last_login_at") private LocalDateTime lastLoginAt;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    public enum UserRole { JOB_SEEKER, RECRUITER, RECRUITER_TEAM, ADMIN }
    public boolean isDeleted() { return deletedAt != null; }
    public void softDelete() { this.deletedAt = LocalDateTime.now(); }
}

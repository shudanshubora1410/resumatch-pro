package com.resumatchpro.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "recruiter_teams", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_owner_id", "member_user_id"})})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecruiterTeam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_owner_id", nullable = false) private User companyOwner;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_user_id", nullable = false) private User memberUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invited_by", nullable = false) private User invitedBy;
    @Column(length = 50) private String role = "RECRUITER_TEAM";
    @Column(name = "is_active") private Boolean isActive = true;
    @CreationTimestamp @Column(name = "joined_at", updatable = false) private LocalDateTime joinedAt;
}

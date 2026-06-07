package com.resumatchpro.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "seeker_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeekerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(length = 200) private String headline;
    @Column(name = "linkedin_url", length = 200) private String linkedinUrl;
    @Column(name = "portfolio_url", length = 200) private String portfolioUrl;
    @Column(name = "github_url", length = 200) private String githubUrl;
    @Column(name = "total_experience_years") private Integer totalExperienceYears = 0;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}

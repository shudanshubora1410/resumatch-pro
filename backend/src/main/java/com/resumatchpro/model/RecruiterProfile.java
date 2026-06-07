package com.resumatchpro.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "recruiter_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecruiterProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(name = "company_name", nullable = false, length = 150) private String companyName;
    @Column(name = "company_email", length = 100) private String companyEmail;
    @Column(length = 100) private String industry;
    @Column(name = "company_size", length = 50) private String companySize;
    @Column(name = "company_description", columnDefinition = "TEXT") private String companyDescription;
    @Column(length = 200) private String website;
    @Column(name = "logo_url", length = 255) private String logoUrl;
    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at") private LocalDateTime updatedAt;
}

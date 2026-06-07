package com.resumatchpro.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "job_listing_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_listing_id", nullable = false)
    private JobListing jobListing;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt = LocalDateTime.now();
}

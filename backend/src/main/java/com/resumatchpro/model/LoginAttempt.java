package com.resumatchpro.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String email;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "is_successful")
    private Boolean isSuccessful;

    @Column(name = "attempt_time", updatable = false)
    private LocalDateTime attemptTime = LocalDateTime.now();
}

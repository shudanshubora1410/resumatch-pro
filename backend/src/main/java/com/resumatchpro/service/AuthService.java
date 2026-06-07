package com.resumatchpro.service;

import com.resumatchpro.config.JwtConfig;
import com.resumatchpro.dto.request.*;
import com.resumatchpro.dto.response.AuthResponse;
import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.security.JwtTokenProvider;
import com.resumatchpro.utility.InputSanitizerUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;
    private final InputSanitizerUtil sanitizer;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = sanitizer.sanitizeEmail(request.getEmail());
        String fullName = sanitizer.sanitizePlainText(request.getFullName());
        if (userRepository.existsByEmail(email)) throw new RuntimeException("Email already registered");
        User.UserRole role;
        try { role = User.UserRole.valueOf(request.getRole().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new RuntimeException("Invalid role. Must be JOB_SEEKER or RECRUITER"); }
        if (role == User.UserRole.ADMIN) throw new UnauthorizedAccessException("Cannot register as admin");

        User user = User.builder().fullName(fullName).email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(sanitizer.sanitizePlainText(request.getPhone()))
                .location(sanitizer.sanitizePlainText(request.getLocation()))
                .role(role).isActive(true).isEmailVerified(false).build();
        user = userRepository.save(user);

        if (role == User.UserRole.RECRUITER) {
            RecruiterProfile profile = RecruiterProfile.builder().user(user)
                    .companyName(sanitizer.sanitizePlainText(request.getCompanyName()))
                    .industry(request.getIndustry()).companySize(request.getCompanySize()).build();
            recruiterProfileRepository.save(profile);
        } else {
            seekerProfileRepository.save(SeekerProfile.builder().user(user).totalExperienceYears(0).build());
        }
        log.info("User registered: {} as {}", email, role);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        RefreshToken refreshToken = RefreshToken.builder().user(user).token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpirationMs() / 1000)).build();
        refreshTokenRepository.save(refreshToken);

        try { emailService.sendWelcomeEmail(user.getEmail(), user.getFullName()); }
        catch (Exception e) { log.warn("Failed to send welcome email to {}: {}", email, e.getMessage()); }

        return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshTokenStr)
                .expiresIn(jwtConfig.getAccessTokenExpirationMs() / 1000).userId(user.getId())
                .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole().name()).build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String email = sanitizer.sanitizeEmail(request.getEmail());
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCK_DURATION_MINUTES);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(email, since);
        if (failedAttempts >= MAX_LOGIN_ATTEMPTS)
            throw new RateLimitExceededException("Too many failed attempts. Try again in " + LOCK_DURATION_MINUTES + " minutes", LOCK_DURATION_MINUTES * 60);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if (!user.getIsActive() || user.isDeleted()) throw new UnauthorizedAccessException("Account is deactivated or deleted");
            user.setLastLoginAt(LocalDateTime.now()); userRepository.save(user);

            loginAttemptRepository.save(LoginAttempt.builder().email(email).ipAddress(ipAddress).isSuccessful(true).build());

            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
            String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
            RefreshToken refreshToken = RefreshToken.builder().user(user).token(refreshTokenStr)
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpirationMs() / 1000)).build();
            refreshTokenRepository.save(refreshToken);
            log.info("User login: {}", email);

            return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshTokenStr)
                    .expiresIn(jwtConfig.getAccessTokenExpirationMs() / 1000).userId(user.getId())
                    .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole().name()).build();
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            loginAttemptRepository.save(LoginAttempt.builder().email(email).ipAddress(ipAddress).isSuccessful(false).build());
            log.warn("Failed login: {} from IP {}", email, ipAddress);
            throw e;
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        if (storedToken.getIsRevoked()) throw new InvalidTokenException("Refresh token revoked");
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) throw new TokenExpiredException("Refresh token expired");
        User user = storedToken.getUser();
        storedToken.setIsRevoked(true); refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        RefreshToken newRefreshToken = RefreshToken.builder().user(user).token(newRefreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpirationMs() / 1000)).build();
        refreshTokenRepository.save(newRefreshToken);

        return AuthResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshTokenStr)
                .expiresIn(jwtConfig.getAccessTokenExpirationMs() / 1000).userId(user.getId())
                .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole().name()).build();
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setIsRevoked(true); refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = sanitizer.sanitizeEmail(request.getEmail());
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder().user(user).token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(15)).build());
            try { emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token); }
            catch (Exception e) { log.error("Failed to send reset email to {}: {}", email, e.getMessage()); }
        });
        log.info("Password reset requested for: {}", email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));
        if (resetToken.getIsUsed()) throw new InvalidTokenException("Token already used");
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) throw new TokenExpiredException("Token expired");
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword())); userRepository.save(user);
        resetToken.setIsUsed(true); passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("Password reset completed for: {}", user.getEmail());
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

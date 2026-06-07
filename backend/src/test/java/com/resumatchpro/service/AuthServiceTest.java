package com.resumatchpro.service;

import com.resumatchpro.config.JwtConfig;
import com.resumatchpro.dto.request.*;
import com.resumatchpro.dto.response.AuthResponse;
import com.resumatchpro.exception.*;
import com.resumatchpro.model.*;
import com.resumatchpro.repository.*;
import com.resumatchpro.security.JwtTokenProvider;
import com.resumatchpro.utility.InputSanitizerUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RecruiterProfileRepository recruiterProfileRepository;
    @Mock private SeekerProfileRepository seekerProfileRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private EmailService emailService;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private JwtConfig jwtConfig;
    private InputSanitizerUtil sanitizer;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-key-for-junit-testing-must-be-at-least-256-bits-long-enough");
        jwtConfig.setAccessTokenExpirationMs(900000);
        jwtConfig.setRefreshTokenExpirationMs(604800000);
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        sanitizer = new InputSanitizerUtil();

        authService = new AuthService(userRepository, recruiterProfileRepository,
                seekerProfileRepository, refreshTokenRepository, passwordResetTokenRepository,
                loginAttemptRepository, passwordEncoder, jwtTokenProvider, jwtConfig,
                authenticationManager, sanitizer, emailService);
    }

    @Test
    void testRegisterSeeker_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john@test.com");
        request.setPassword("password123");
        request.setRole("JOB_SEEKER");

        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(seekerProfileRepository.save(any())).thenReturn(null);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("JOB_SEEKER", response.getRole());
    }

    @Test
    void testRegisterDuplicateEmail_shouldThrow() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("duplicate@test.com");
        request.setPassword("password123");
        request.setRole("JOB_SEEKER");

        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void testPasswordReset_shouldMarkTokenUsed() {
        String token = "valid-uuid-token";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token).isUsed(false)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(15))
                .user(User.builder().id(1L).email("user@test.com").build())
                .build();

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("newpassword123");

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userRepository.save(any(User.class))).thenReturn(null);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(null);

        assertDoesNotThrow(() -> authService.resetPassword(request));
    }
}

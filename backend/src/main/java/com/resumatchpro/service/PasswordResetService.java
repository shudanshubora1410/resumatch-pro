package com.resumatchpro.service;

import com.resumatchpro.model.PasswordResetToken;
import com.resumatchpro.model.User;
import com.resumatchpro.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken create(User user) {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();
        return passwordResetTokenRepository.save(token);
    }

    public PasswordResetToken validate(String tokenStr) {
        return passwordResetTokenRepository.findByToken(tokenStr).orElse(null);
    }

    public void markUsed(PasswordResetToken token) {
        token.setIsUsed(true);
        passwordResetTokenRepository.save(token);
    }
}

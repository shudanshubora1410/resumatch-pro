package com.resumatchpro.utility;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

@Component
public class InputSanitizerUtil {

    private final PolicyFactory policy;

    public InputSanitizerUtil() {
        this.policy = Sanitizers.FORMATTING
                .and(Sanitizers.BLOCKS)
                .and(Sanitizers.LINKS)
                .and(Sanitizers.TABLES);
    }

    public String sanitize(String input) {
        if (input == null) return null;
        return policy.sanitize(input);
    }

    public String sanitizePlainText(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "")
                    .replaceAll("[<>\"';&]", "")
                    .trim();
    }

    public String sanitizeEmail(String email) {
        if (email == null) return null;
        return email.toLowerCase().trim().replaceAll("[^a-zA-Z0-9@._+-]", "");
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

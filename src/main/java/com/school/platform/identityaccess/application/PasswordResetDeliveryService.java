package com.school.platform.identityaccess.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PasswordResetDeliveryService {
    private final String resetUrl;
    private final boolean logResetLink;
    private final Environment environment;

    public PasswordResetDeliveryService(
            @Value("${app.frontend.reset-password-url:http://localhost:4200/auth/reset-password}") String resetUrl,
            @Value("${app.password-reset.log-link:false}") boolean logResetLink,
            Environment environment
    ) {
        this.resetUrl = resetUrl;
        this.logResetLink = logResetLink;
        this.environment = environment;
    }

    public void sendResetToken(String email, String rawToken) {
        if (logResetLink && !isProdProfileActive()) {
            log.warn("Password reset link for {}: {}", email, buildResetLink(email, rawToken));
            return;
        }
        if (logResetLink) {
            log.warn("Password reset link logging is disabled in production for {}", email);
        }
        log.info("Password reset requested for {}. Configure an email provider to deliver the reset link.", email);
    }

    private String buildResetLink(String email, String rawToken) {
        return resetUrl
                + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    private boolean isProdProfileActive() {
        String[] profiles = environment.getActiveProfiles();
        return profiles != null && Arrays.stream(profiles).anyMatch("prod"::equalsIgnoreCase);
    }
}

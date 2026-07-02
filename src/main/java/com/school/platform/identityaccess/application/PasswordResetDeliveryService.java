package com.school.platform.identityaccess.application;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PasswordResetDeliveryService {
    private final String resetUrl;
    private final boolean logResetLink;

    public PasswordResetDeliveryService(
            @Value("${app.frontend.reset-password-url:http://localhost:4200/auth/reset-password}") String resetUrl,
            @Value("${app.password-reset.log-link:false}") boolean logResetLink
    ) {
        this.resetUrl = resetUrl;
        this.logResetLink = logResetLink;
    }

    public void sendResetToken(String email, String rawToken) {
        String link = resetUrl
                + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        if (logResetLink) {
            log.warn("Password reset link for {}: {}", email, link);
            return;
        }
        log.info("Password reset requested for {}. Configure an email provider to deliver the reset link.", email);
    }
}

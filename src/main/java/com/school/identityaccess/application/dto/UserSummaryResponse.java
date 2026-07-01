package com.school.identityaccess.application.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserSummaryResponse(
        Long id,
        String username,
        String fullName,
        String email,
        boolean active,
        LocalDateTime lastLoginAt,
        Set<String> authorities
) {
}

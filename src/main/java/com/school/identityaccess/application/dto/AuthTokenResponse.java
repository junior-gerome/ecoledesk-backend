package com.school.identityaccess.application.dto;

import java.time.Instant;

public record AuthTokenResponse(
        String accessToken,
        Instant expiresAt,
        UserSummaryResponse user
) {
}

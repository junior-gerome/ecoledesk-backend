package com.school.platform.support.application.dto;

import java.time.LocalDateTime;

public record SupportTicketDTO(
        long id,
        SupportTicketStatus status,
        LocalDateTime createdAt
) {
}

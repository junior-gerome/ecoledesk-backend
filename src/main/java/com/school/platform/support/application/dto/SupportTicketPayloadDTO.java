package com.school.platform.support.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupportTicketPayloadDTO(
        @NotBlank @Size(max = 120) String name,
        @Email @NotBlank @Size(max = 150) String email,
        @NotBlank @Size(max = 160) String subject,
        @NotBlank @Size(max = 2000) String message,
        @NotNull SupportTicketPriority priority
) {
}

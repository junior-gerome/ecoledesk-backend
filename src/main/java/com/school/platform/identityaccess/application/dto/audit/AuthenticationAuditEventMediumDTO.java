package com.school.platform.identityaccess.application.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationAuditEventMediumDTO {
    private Long id;
    private Long userId;
    private String username;
    private String eventType;
    private Boolean successful;
    private String reason;
    private String clientIp;
    private LocalDateTime createdAt;
}

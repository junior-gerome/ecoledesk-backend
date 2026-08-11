package com.school.platform.identityaccess.application.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationAuditEventBasicDTO {
    private Long id;
    private String username;
    private String eventType;
    private Boolean successful;
    private LocalDateTime createdAt;
}

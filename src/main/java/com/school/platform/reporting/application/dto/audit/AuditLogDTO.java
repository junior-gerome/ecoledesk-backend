package com.school.platform.reporting.application.dto.audit;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String username;
    private String action;
    private LocalDateTime dateAction;
    private String ipAdresse;
    private String tableCible;
    private Long referenceId;
}

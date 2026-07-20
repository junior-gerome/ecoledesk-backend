package com.school.platform.reporting.web;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.reporting.application.dto.audit.AuditLogDTO;
import com.school.platform.reporting.domain.model.LogActivite;
import com.school.platform.reporting.infrastructure.persistence.LogActiviteRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {
    private final LogActiviteRepository logActiviteRepository;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getRecentLogs(
            @RequestParam(defaultValue = "50") int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 200));
        List<AuditLogDTO> logs = logActiviteRepository
                .findAllByOrderByDateActionDesc(PageRequest.of(0, resolvedLimit))
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(logs);
    }

    private AuditLogDTO toDto(LogActivite log) {
        String username = log.getUserAccount() != null ? log.getUserAccount().getUsername() : null;
        return new AuditLogDTO(
                log.getId(),
                username,
                log.getAction(),
                log.getDateAction(),
                log.getIpAdresse(),
                log.getTableCible(),
                log.getReferenceId());
    }
}

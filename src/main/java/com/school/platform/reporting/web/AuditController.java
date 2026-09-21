package com.school.platform.reporting.web;

import org.springframework.data.domain.Page;
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
import com.school.platform.shared.web.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {
    private final LogActiviteRepository logActiviteRepository;

    /**
     * GET /audit/logs — journal d'audit pagine.
     * Parametres : page (defaut 0), size (defaut 50, cap 200).
     * Le parametre historique {@code limit} reste accepte comme alias de
     * {@code size} pour ne pas casser les anciens appels.
     * Retour : {@code PageResponse<AuditLogDTO>} (contrat standard du projet).
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogDTO>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit) {
        int requestedSize = size != null ? size : (limit != null ? limit : 50);
        int resolvedSize = Math.min(Math.max(requestedSize, 1), 200);
        Page<AuditLogDTO> result = logActiviteRepository
                .findAllByOrderByDateActionDesc(PageRequest.of(Math.max(page, 0), resolvedSize))
                .map(this::toDto);

        return ResponseEntity.ok(PageResponse.from(result));
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

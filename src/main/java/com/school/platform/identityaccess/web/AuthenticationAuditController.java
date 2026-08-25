package com.school.platform.identityaccess.web;

import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventFullDTO;
import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventMediumDTO;
import com.school.platform.identityaccess.application.interfaces.IAuthenticationAuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authentication-audit-events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuthenticationAuditController {

    private final IAuthenticationAuditQueryService auditQueryService;

    @GetMapping
    public ResponseEntity<Page<AuthenticationAuditEventMediumDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(auditQueryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthenticationAuditEventFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(auditQueryService.findById(id));
    }
}

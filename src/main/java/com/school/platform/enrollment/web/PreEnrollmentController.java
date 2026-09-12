package com.school.platform.enrollment.web;

import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentDocumentRequest;
import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentGuardianRequest;
import com.school.platform.enrollment.application.dto.preenrollment.CreatePreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.preenrollment.DecisionRequest;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentResponse;
import com.school.platform.enrollment.application.dto.preenrollment.ReviewPreEnrollmentDocumentRequest;
import com.school.platform.enrollment.application.preenrollment.PreEnrollmentCommandService;
import com.school.platform.shared.application.BusinessAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pre-enrollments")
@RequiredArgsConstructor
public class PreEnrollmentController {

    private final PreEnrollmentCommandService service;
    private final BusinessAuditService auditService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> createDraft(
            @Valid @RequestBody CreatePreEnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDraft(request));
    }

    @PostMapping("/{id}/guardians")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> addGuardian(
            @PathVariable Long id,
            @Valid @RequestBody AddPreEnrollmentGuardianRequest request) {
        return ResponseEntity.ok(service.addGuardian(id, request));
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> addDocument(
            @PathVariable Long id,
            @Valid @RequestBody AddPreEnrollmentDocumentRequest request) {
        return ResponseEntity.ok(service.addDocument(id, request));
    }

    @PostMapping("/{id}/documents/{documentId}/review")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> reviewDocument(
            @PathVariable Long id,
            @PathVariable Long documentId,
            @Valid @RequestBody ReviewPreEnrollmentDocumentRequest request) {
        Long reviewerId = auditorId();
        return ResponseEntity.ok(service.reviewDocument(
                id, documentId, request.getStatus(), reviewerId, request.getReason()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/start-review")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(service.startReview(id, auditorId()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id, auditorId()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('SECRETAIRE') or hasRole('DIRECTION')")
    public ResponseEntity<PreEnrollmentResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DecisionRequest request) {
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(service.reject(id, auditorId(), reason));
    }

    private Long auditorId() {
        return auditService.currentUserId().orElse(null);
    }
}
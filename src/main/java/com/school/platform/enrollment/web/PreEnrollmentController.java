package com.school.platform.enrollment.web;

import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentGuardianRequest;
import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentDocumentRequest;
import com.school.platform.enrollment.application.dto.preenrollment.CreatePreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.preenrollment.DecisionRequest;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentResponse;
import com.school.platform.enrollment.application.dto.preenrollment.ReviewPreEnrollmentDocumentRequest;
import com.school.platform.enrollment.application.preenrollment.PreEnrollmentCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pre-enrollments")
@RequiredArgsConstructor
public class PreEnrollmentController {

    private final PreEnrollmentCommandService service;

    @PostMapping
    public ResponseEntity<PreEnrollmentResponse> createDraft(
            @Valid @RequestBody CreatePreEnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDraft(request));
    }

    @PostMapping("/{id}/guardians")
    public ResponseEntity<PreEnrollmentResponse> addGuardian(
            @PathVariable Long id,
            @RequestBody AddPreEnrollmentGuardianRequest request) {
        return ResponseEntity.ok(service.addGuardian(id, request));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<PreEnrollmentResponse> addDocument(
            @PathVariable Long id,
            @RequestBody AddPreEnrollmentDocumentRequest request) {
        return ResponseEntity.ok(service.addDocument(id, request));
    }

    @PostMapping("/{id}/documents/{documentId}/review")
    public ResponseEntity<PreEnrollmentResponse> reviewDocument(
            @PathVariable Long id,
            @PathVariable Long documentId,
            @RequestBody ReviewPreEnrollmentDocumentRequest request) {
        return ResponseEntity.ok(service.reviewDocument(
                id, documentId, request.getStatus(), request.getReviewedBy(), request.getReason()));
    }
    @PostMapping("/{id}/submit")
    public ResponseEntity<PreEnrollmentResponse> submit(@PathVariable Long id) {
        return ResponseEntity.ok(service.submit(id));
    }

    @PostMapping("/{id}/start-review")
    public ResponseEntity<PreEnrollmentResponse> startReview(
            @PathVariable Long id,
            @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(service.startReview(id, request.getReviewedBy()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PreEnrollmentResponse> approve(
            @PathVariable Long id,
            @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(service.approve(id, request.getReviewedBy()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PreEnrollmentResponse> reject(
            @PathVariable Long id,
            @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(service.reject(id, request.getReviewedBy(), request.getReason()));
    }
}
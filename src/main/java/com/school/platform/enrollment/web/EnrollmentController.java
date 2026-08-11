package com.school.platform.enrollment.web;

import com.school.platform.enrollment.application.dto.enrollment.CreateEnrollmentFromPreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentResponse;
import com.school.platform.enrollment.application.enrollment.EnrollmentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentCommandService enrollmentCommandService;

    @PostMapping("/from-pre-enrollment/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<EnrollmentResponse> create(
            @PathVariable Long id,
            @RequestBody CreateEnrollmentFromPreEnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentCommandService.createFromApprovedPreEnrollment(id, request));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<EnrollmentResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentCommandService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<EnrollmentResponse> cancel(
            @PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(enrollmentCommandService.cancel(id, reason));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<EnrollmentResponse> withdraw(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentCommandService.withdraw(id));
    }
}

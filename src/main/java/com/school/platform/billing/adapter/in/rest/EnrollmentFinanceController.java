package com.school.platform.billing.adapter.in.rest;

import com.school.platform.billing.application.PreEnrollmentFeePaymentService;
import com.school.platform.billing.application.TuitionPaymentPlanService;
import com.school.platform.billing.application.dto.CreateTuitionPaymentPlanRequest;
import com.school.platform.billing.application.dto.PreEnrollmentFeePaymentResponse;
import com.school.platform.billing.application.dto.RecordPreEnrollmentFeePaymentRequest;
import com.school.platform.billing.application.dto.TuitionPaymentPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollment-finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
public class EnrollmentFinanceController {

    private final PreEnrollmentFeePaymentService feePaymentService;
    private final TuitionPaymentPlanService paymentPlanService;

    @PostMapping("/pre-enrollments/{id}/fee-payments")
    public ResponseEntity<PreEnrollmentFeePaymentResponse> recordPreEnrollmentFee(
            @PathVariable Long id,
            @RequestBody RecordPreEnrollmentFeePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feePaymentService.record(id, request));
    }

    @PostMapping("/pre-enrollment-fee-payments/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreEnrollmentFeePaymentResponse> verifyPreEnrollmentFee(@PathVariable Long id) {
        return ResponseEntity.ok(feePaymentService.verify(id));
    }

    @PostMapping("/enrollments/{id}/tuition-payment-plan")
    public ResponseEntity<TuitionPaymentPlanResponse> createTuitionPaymentPlan(
            @PathVariable Long id,
            @RequestBody CreateTuitionPaymentPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentPlanService.create(id, request));
    }

    @GetMapping("/enrollments/{id}/tuition-payment-plan")
    public ResponseEntity<TuitionPaymentPlanResponse> getTuitionPaymentPlan(@PathVariable Long id) {
        return ResponseEntity.ok(paymentPlanService.findByEnrollment(id));
    }
}
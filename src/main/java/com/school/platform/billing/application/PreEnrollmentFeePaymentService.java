package com.school.platform.billing.application;

import com.school.platform.billing.application.dto.PreEnrollmentFeePaymentResponse;
import com.school.platform.billing.application.dto.RecordPreEnrollmentFeePaymentRequest;
import com.school.platform.billing.domain.model.PreEnrollmentFeePayment;
import com.school.platform.billing.infrastructure.persistence.PreEnrollmentFeePaymentRepository;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreEnrollmentFeePaymentService {

    private final PreEnrollmentFeePaymentRepository paymentRepository;
    private final PreEnrollmentRepository preEnrollmentRepository;

    @Transactional
    public PreEnrollmentFeePaymentResponse record(Long preEnrollmentId, RecordPreEnrollmentFeePaymentRequest request) {
        PreEnrollment preEnrollment = preEnrollmentRepository.findById(preEnrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("PreEnrollment", "id", preEnrollmentId));
        if (preEnrollment.getStatus() == PreEnrollmentStatus.CANCELLED
                || preEnrollment.getStatus() == PreEnrollmentStatus.REJECTED
                || preEnrollment.getStatus() == PreEnrollmentStatus.EXPIRED) {
            throw new BadRequestException("A fee payment cannot be recorded for a closed pre-enrollment");
        }
        if (paymentRepository.existsByTransactionReference(request.getTransactionReference())) {
            throw new BadRequestException("The transaction reference already exists");
        }

        PreEnrollmentFeePayment payment = PreEnrollmentFeePayment.recorded(
                preEnrollment,
                request.getAmount(),
                request.getPaymentDate(),
                request.getTransactionReference(),
                request.getReceiptNumber());
        return response(paymentRepository.save(payment));
    }

    @Transactional
    public PreEnrollmentFeePaymentResponse verify(Long paymentId) {
        PreEnrollmentFeePayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PreEnrollmentFeePayment", "id", paymentId));
        payment.verify();
        return response(paymentRepository.save(payment));
    }

    private PreEnrollmentFeePaymentResponse response(PreEnrollmentFeePayment payment) {
        return PreEnrollmentFeePaymentResponse.builder()
                .id(payment.getId())
                .preEnrollmentId(payment.getPreEnrollment().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .transactionReference(payment.getTransactionReference())
                .receiptNumber(payment.getReceiptNumber())
                .verified(payment.isVerified())
                .build();
    }
}
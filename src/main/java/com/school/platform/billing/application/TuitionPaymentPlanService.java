package com.school.platform.billing.application;

import com.school.platform.billing.application.dto.CreateTuitionPaymentPlanRequest;
import com.school.platform.billing.application.dto.PaymentInstallmentRequest;
import com.school.platform.billing.application.dto.TuitionPaymentPlanResponse;
import com.school.platform.billing.domain.model.PaymentInstallment;
import com.school.platform.billing.domain.model.TuitionPaymentPlan;
import com.school.platform.billing.infrastructure.persistence.TuitionPaymentPlanRepository;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TuitionPaymentPlanService {

    private final TuitionPaymentPlanRepository planRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public TuitionPaymentPlanResponse create(Long enrollmentId, CreateTuitionPaymentPlanRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));
        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED) {
            throw new BadRequestException("A payment plan can only be created for a confirmed enrollment");
        }
        if (planRepository.existsByEnrollmentId(enrollmentId)) {
            throw new BadRequestException("An active payment plan already exists for this enrollment");
        }

        BigDecimal discount = request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount();
        TuitionPaymentPlan plan = TuitionPaymentPlan.draft(enrollment, request.getTotalAmount(), discount);
        List<PaymentInstallmentRequest> installments = request.getInstallments();
        if (installments == null || installments.isEmpty()) {
            throw new BadRequestException("At least one installment is required");
        }
        for (PaymentInstallmentRequest installment : installments) {
            plan.addInstallment(installment.getAmount(), installment.getDueDate());
        }
        plan.activate();
        return response(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public TuitionPaymentPlanResponse findByEnrollment(Long enrollmentId) {
        TuitionPaymentPlan plan = planRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("TuitionPaymentPlan", "enrollmentId", enrollmentId));
        return response(plan);
    }

    private TuitionPaymentPlanResponse response(TuitionPaymentPlan plan) {
        List<TuitionPaymentPlanResponse.Installment> installments = plan.getInstallments().stream()
                .map(this::installmentResponse)
                .toList();
        return TuitionPaymentPlanResponse.builder()
                .id(plan.getId())
                .enrollmentId(plan.getEnrollment().getId())
                .totalAmount(plan.getTotalAmount())
                .discountAmount(plan.getDiscountAmount())
                .netAmount(plan.getNetAmount())
                .status(plan.getStatus())
                .installments(installments)
                .build();
    }

    private TuitionPaymentPlanResponse.Installment installmentResponse(PaymentInstallment installment) {
        return TuitionPaymentPlanResponse.Installment.builder()
                .sequenceNumber(installment.getSequenceNumber())
                .expectedAmount(installment.getExpectedAmount())
                .dueDate(installment.getDueDate())
                .build();
    }
}
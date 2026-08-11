package com.school.platform.billing.application.dto;

import com.school.platform.billing.domain.model.PaymentPlanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TuitionPaymentPlanResponse {
    private Long id;
    private Long enrollmentId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private PaymentPlanStatus status;
    private List<Installment> installments;

    @Data
    @Builder
    public static class Installment {
        private int sequenceNumber;
        private BigDecimal expectedAmount;
        private LocalDate dueDate;
    }
}
package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CreateTuitionPaymentPlanRequest {
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private List<PaymentInstallmentRequest> installments;
}
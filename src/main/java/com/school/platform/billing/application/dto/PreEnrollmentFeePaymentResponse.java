package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreEnrollmentFeePaymentResponse {
    private Long id;
    private Long preEnrollmentId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String transactionReference;
    private String receiptNumber;
    private boolean verified;
}
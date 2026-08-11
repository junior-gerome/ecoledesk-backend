package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RecordPreEnrollmentFeePaymentRequest {
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String transactionReference;
    private String receiptNumber;
}
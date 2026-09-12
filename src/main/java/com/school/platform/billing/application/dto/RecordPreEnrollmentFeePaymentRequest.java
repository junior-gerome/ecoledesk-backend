package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RecordPreEnrollmentFeePaymentRequest {
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String transactionReference;
    private String receiptNumber;
}
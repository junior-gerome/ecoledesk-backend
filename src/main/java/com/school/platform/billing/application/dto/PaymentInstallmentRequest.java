package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PaymentInstallmentRequest {
    private BigDecimal amount;
    private LocalDate dueDate;
}
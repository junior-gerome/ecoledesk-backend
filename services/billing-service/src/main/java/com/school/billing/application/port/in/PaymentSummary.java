package com.school.billing.application.port.in;

import java.math.BigDecimal;

public record PaymentSummary(
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        BigDecimal lateAmount
) {
}

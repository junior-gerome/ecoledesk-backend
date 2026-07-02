package com.school.billing.adapter.in.rest;

import com.school.billing.domain.model.PaymentMethod;
import com.school.billing.domain.model.PaymentStatus;
import com.school.billing.domain.model.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long studentId,
        String studentName,
        BigDecimal amount,
        LocalDate paymentDate,
        LocalDate dueDate,
        PaymentType type,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String receiptNumber,
        String description
) {
}

package com.school.billing.adapter.in.rest;

import com.school.billing.domain.model.PaymentMethod;
import com.school.billing.domain.model.PaymentStatus;
import com.school.billing.domain.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotNull Long studentId,
        @Size(max = 160) String studentName,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotNull LocalDate paymentDate,
        @NotNull LocalDate dueDate,
        @NotNull PaymentType type,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        @Size(max = 80) String receiptNumber,
        @Size(max = 1000) String description
) {
}

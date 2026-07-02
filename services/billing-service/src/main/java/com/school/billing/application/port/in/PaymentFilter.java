package com.school.billing.application.port.in;

import com.school.billing.domain.model.PaymentStatus;
import com.school.billing.domain.model.PaymentType;
import java.time.LocalDate;

public record PaymentFilter(
        Long studentId,
        PaymentStatus status,
        PaymentType type,
        LocalDate startDate,
        LocalDate endDate
) {
}

package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One payment row as written into an Excel or PDF export file.
 */
public record PaymentExportRow(
        Long id,
        String receiptNumber,
        String studentName,
        String studentNumber,
        String className,
        String type,
        BigDecimal amount,
        BigDecimal discount,
        BigDecimal remainingAmount,
        String status,
        LocalDate paymentDate,
        LocalDate dueDate,
        String paymentMethod,
        String description) {
}
package com.school.billing.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Payment(
        Long id,
        Long studentId,
        String studentName,
        Money amount,
        LocalDate paymentDate,
        LocalDate dueDate,
        PaymentType type,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String receiptNumber,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Payment {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("A valid student id is required.");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date is required.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Payment type is required.");
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.CASH;
        }
    }

    public Payment markLateIfOverdue(LocalDate businessDate) {
        if (status == PaymentStatus.PENDING && dueDate.isBefore(businessDate)) {
            return withStatus(PaymentStatus.LATE);
        }
        return this;
    }

    public Payment withStatus(PaymentStatus nextStatus) {
        return new Payment(id, studentId, studentName, amount, paymentDate, dueDate, type,
                nextStatus, paymentMethod, receiptNumber, description, createdAt, updatedAt);
    }
}

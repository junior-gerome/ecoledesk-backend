package com.school.billing.adapter.in.rest;

import com.school.billing.domain.model.Money;
import com.school.billing.domain.model.Payment;

final class PaymentRestMapper {
    private PaymentRestMapper() {
    }

    static Payment toDomain(PaymentRequest request) {
        return new Payment(null, request.studentId(), request.studentName(), Money.of(request.amount()),
                request.paymentDate(), request.dueDate(), request.type(), request.status(),
                request.paymentMethod(), request.receiptNumber(), request.description(), null, null);
    }

    static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.id(), payment.studentId(), payment.studentName(),
                payment.amount().amount(), payment.paymentDate(), payment.dueDate(), payment.type(),
                payment.status(), payment.paymentMethod(), payment.receiptNumber(), payment.description());
    }
}

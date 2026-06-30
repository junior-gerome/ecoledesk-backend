package com.school.billing.adapter.out.persistence;

import com.school.billing.domain.model.Money;
import com.school.billing.domain.model.Payment;

final class PaymentPersistenceMapper {
    private PaymentPersistenceMapper() {
    }

    static Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(entity.id, entity.studentId, entity.studentName, Money.of(entity.amount),
                entity.paymentDate, entity.dueDate, entity.type, entity.status, entity.paymentMethod,
                entity.receiptNumber, entity.description, entity.createdAt, entity.updatedAt);
    }

    static PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.id = payment.id();
        entity.studentId = payment.studentId();
        entity.studentName = payment.studentName();
        entity.amount = payment.amount().amount();
        entity.paymentDate = payment.paymentDate();
        entity.dueDate = payment.dueDate();
        entity.type = payment.type();
        entity.status = payment.status();
        entity.paymentMethod = payment.paymentMethod();
        entity.receiptNumber = payment.receiptNumber();
        entity.description = payment.description();
        return entity;
    }
}

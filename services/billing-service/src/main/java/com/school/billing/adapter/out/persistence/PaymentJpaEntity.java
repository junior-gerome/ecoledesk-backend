package com.school.billing.adapter.out.persistence;

import com.school.billing.domain.model.PaymentMethod;
import com.school.billing.domain.model.PaymentStatus;
import com.school.billing.domain.model.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payments")
class PaymentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    Long studentId;
    String studentName;
    @Column(nullable = false)
    BigDecimal amount;
    @Column(nullable = false)
    LocalDate paymentDate;
    @Column(nullable = false)
    LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;
    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;
    String receiptNumber;
    String description;
    @CreationTimestamp
    LocalDateTime createdAt;
    @UpdateTimestamp
    LocalDateTime updatedAt;
}

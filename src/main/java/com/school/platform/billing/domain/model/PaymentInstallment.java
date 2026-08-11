package com.school.platform.billing.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "payment_installments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_installment_sequence",
                columnNames = {"plan_id", "sequence_number"}))
@Getter
@NoArgsConstructor
public class PaymentInstallment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private TuitionPaymentPlan plan;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "expected_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InstallmentStatus status = InstallmentStatus.PENDING;

    static PaymentInstallment pending(
            TuitionPaymentPlan plan, int sequenceNumber, BigDecimal expectedAmount, LocalDate dueDate) {
        if (sequenceNumber < 1 || expectedAmount == null || expectedAmount.signum() <= 0 || dueDate == null) {
            throw new IllegalArgumentException("Invalid payment installment");
        }
        PaymentInstallment installment = new PaymentInstallment();
        installment.plan = plan;
        installment.sequenceNumber = sequenceNumber;
        installment.expectedAmount = expectedAmount;
        installment.dueDate = dueDate;
        return installment;
    }
}
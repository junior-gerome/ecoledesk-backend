package com.school.platform.billing.domain.model;

import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pre_enrollment_fee_payments")
@Getter
@NoArgsConstructor
public class PreEnrollmentFeePayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_enrollment_id", nullable = false)
    private PreEnrollment preEnrollment;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean refundable;

    public static PreEnrollmentFeePayment recorded(
            PreEnrollment preEnrollment,
            BigDecimal amount,
            LocalDateTime paymentDate,
            String transactionReference,
            String receiptNumber) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (transactionReference == null || transactionReference.isBlank()) {
            throw new IllegalArgumentException("Transaction reference is required");
        }

        PreEnrollmentFeePayment payment = new PreEnrollmentFeePayment();
        payment.preEnrollment = preEnrollment;
        payment.amount = amount;
        payment.paymentDate = paymentDate == null ? LocalDateTime.now() : paymentDate;
        payment.transactionReference = transactionReference.trim();
        payment.receiptNumber = receiptNumber;
        return payment;
    }

    public void verify() {
        if (verified) {
            throw new IllegalStateException("Payment is already verified");
        }
        verified = true;
    }
}
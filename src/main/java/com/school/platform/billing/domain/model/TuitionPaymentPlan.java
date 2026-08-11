package com.school.platform.billing.domain.model;

import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tuition_payment_plans")
@Getter
@NoArgsConstructor
public class TuitionPaymentPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentPlanStatus status = PaymentPlanStatus.DRAFT;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PaymentInstallment> installments = new ArrayList<>();

    public static TuitionPaymentPlan draft(Enrollment enrollment, BigDecimal total, BigDecimal discount) {
        if (enrollment == null || total == null || total.signum() < 0
                || discount == null || discount.signum() < 0 || discount.compareTo(total) > 0) {
            throw new IllegalArgumentException("Invalid plan amounts");
        }
        TuitionPaymentPlan plan = new TuitionPaymentPlan();
        plan.enrollment = enrollment;
        plan.totalAmount = total;
        plan.discountAmount = discount;
        plan.netAmount = total.subtract(discount);
        return plan;
    }

    public void addInstallment(BigDecimal expectedAmount, LocalDate dueDate) {
        if (status != PaymentPlanStatus.DRAFT) {
            throw new IllegalStateException("Only a draft plan can be changed");
        }
        installments.add(PaymentInstallment.pending(this, installments.size() + 1, expectedAmount, dueDate));
    }

    public void activate() {
        if (status != PaymentPlanStatus.DRAFT || installments.isEmpty()) {
            throw new IllegalStateException("A draft payment plan needs installments");
        }
        BigDecimal scheduled = installments.stream()
                .map(PaymentInstallment::getExpectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (scheduled.compareTo(netAmount) != 0) {
            throw new IllegalStateException("Installments must equal the net amount");
        }
        status = PaymentPlanStatus.ACTIVE;
    }
}
package com.school.platform.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.school.platform.enrollment.domain.enrollment.Enrollment;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TuitionPaymentPlanTest {

    @Test
    void activatesOnlyWhenDynamicInstallmentsCoverTheNetAmount() {
        TuitionPaymentPlan plan = TuitionPaymentPlan.draft(
                new Enrollment(), new BigDecimal("150000"), new BigDecimal("10000"));

        plan.addInstallment(new BigDecimal("50000"), LocalDate.of(2026, 9, 5));
        plan.addInstallment(new BigDecimal("90000"), LocalDate.of(2026, 11, 5));
        plan.activate();

        assertThat(plan.getStatus()).isEqualTo(PaymentPlanStatus.ACTIVE);
        assertThat(plan.getInstallments()).hasSize(2);
        assertThat(plan.getInstallments()).extracting(PaymentInstallment::getSequenceNumber)
                .containsExactly(1, 2);
    }

    @Test
    void rejectsAPlanWhenInstallmentsDoNotMatchTheNetAmount() {
        TuitionPaymentPlan plan = TuitionPaymentPlan.draft(
                new Enrollment(), new BigDecimal("150000"), BigDecimal.ZERO);
        plan.addInstallment(new BigDecimal("100000"), LocalDate.of(2026, 9, 5));

        assertThatThrownBy(plan::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Installments must equal");
    }
}
package com.school.platform.billing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.school.platform.billing.domain.model.Paiement;
import com.school.platform.billing.infrastructure.persistence.MontantRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;

class PaymentServicePaginationTest {

    private final PaiementRepository paiementRepository = mock(PaiementRepository.class);
    private final StudentRepository studentRepository = mock(StudentRepository.class);
    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
    private final MontantRepository montantRepository = mock(MontantRepository.class);
    private final PaymentService service =
            new PaymentService(paiementRepository, studentRepository, enrollmentRepository, montantRepository);

    private Paiement payment(long id, BigDecimal paid, BigDecimal remaining, String receiptNumber, boolean cancelled) {
        Paiement payment = new Paiement();
        payment.setId(id);
        payment.setMontantPaye(paid);
        payment.setMontantRestant(remaining);
        payment.setReceiptNumber(receiptNumber);
        payment.setDatePaiement(LocalDate.of(2026, 1, 10));
        payment.setDueDate(LocalDate.now().plusDays(30));
        if (cancelled) {
            payment.setCancelledAt(LocalDateTime.now());
        }
        return payment;
    }

    @Test
    void listPaymentsPageNormalizesStatusAndForwardsToSearchPage() {
        Paiement paid = payment(1L, BigDecimal.valueOf(100), BigDecimal.ZERO, "PAY-1", false);
        Pageable pageable = PageRequest.of(0, 20);
        when(paiementRepository.searchPage(
                eq(7L), isNull(), eq("PAID"), isNull(), isNull(), eq("ada"), eq("REC"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(paid), pageable, 1));

        Page<Map<String, Object>> result =
                service.listPaymentsPage(7L, "  paid ", null, null, null, "ada", "REC", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0))
                .containsEntry("id", 1L)
                .containsEntry("status", "PAID")
                .containsEntry("amount", BigDecimal.valueOf(100));
    }

    @Test
    void listPaymentsPageForwardsBlankStatusAsNull() {
        Pageable pageable = PageRequest.of(2, 10);
        when(paiementRepository.searchPage(
                eq(7L), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<Map<String, Object>> result =
                service.listPaymentsPage(7L, "   ", null, null, null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
        verify(paiementRepository)
                .searchPage(eq(7L), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void summarizePaymentsIncludesServerSideReceiptMetrics() {
        Paiement paid = payment(1L, BigDecimal.valueOf(100), BigDecimal.ZERO, "PAY-1", false);
        Paiement pending = payment(2L, BigDecimal.ZERO, BigDecimal.valueOf(50), "PAY-2", false);
        Paiement cancelled = payment(3L, BigDecimal.valueOf(999), BigDecimal.valueOf(999), "PAY-3", true);
        Paiement paidNoReceipt = payment(4L, BigDecimal.valueOf(200), BigDecimal.ZERO, null, false);
        when(paiementRepository.search(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(paid, pending, cancelled, paidNoReceipt));

        Map<String, BigDecimal> summary = service.summarizePayments();

        assertThat(summary.get("totalAmount")).isEqualByComparingTo("350");
        assertThat(summary.get("paidAmount")).isEqualByComparingTo("300");
        assertThat(summary.get("pendingAmount")).isEqualByComparingTo("50");
        assertThat(summary.get("lateAmount")).isEqualByComparingTo("0");
        assertThat(summary.get("totalReceipts")).isEqualByComparingTo("4");
        assertThat(summary.get("numberedReceipts")).isEqualByComparingTo("3");
        assertThat(summary.get("pendingReceiptGeneration")).isEqualByComparingTo("1");
    }
}
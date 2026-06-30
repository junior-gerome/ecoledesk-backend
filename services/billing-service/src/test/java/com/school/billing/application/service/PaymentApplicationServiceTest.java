package com.school.billing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.school.billing.application.port.in.PaymentFilter;
import com.school.billing.application.port.out.PaymentRepository;
import com.school.billing.domain.model.Money;
import com.school.billing.domain.model.Payment;
import com.school.billing.domain.model.PaymentMethod;
import com.school.billing.domain.model.PaymentStatus;
import com.school.billing.domain.model.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PaymentApplicationServiceTest {
    private final PaymentRepository repository = Mockito.mock(PaymentRepository.class);
    private final PaymentApplicationService service = new PaymentApplicationService(repository);

    @Test
    void summarizeGroupsAmountsByStatus() {
        when(repository.findAll(new PaymentFilter(null, null, null, null, null))).thenReturn(List.of(
                payment(1L, PaymentStatus.PAID, "1000"),
                payment(2L, PaymentStatus.PENDING, "500"),
                payment(3L, PaymentStatus.LATE, "250")
        ));

        var summary = service.summarize();

        assertThat(summary.totalAmount()).isEqualByComparingTo("1750.00");
        assertThat(summary.paidAmount()).isEqualByComparingTo("1000.00");
        assertThat(summary.pendingAmount()).isEqualByComparingTo("500.00");
        assertThat(summary.lateAmount()).isEqualByComparingTo("250.00");
    }

    private Payment payment(Long id, PaymentStatus status, String amount) {
        return new Payment(id, 10L, "Ada", Money.of(new BigDecimal(amount)), LocalDate.now(),
                LocalDate.now().plusDays(10), PaymentType.FRAIS_SCOLAIRE, status,
                PaymentMethod.CASH, null, null, null, null);
    }
}

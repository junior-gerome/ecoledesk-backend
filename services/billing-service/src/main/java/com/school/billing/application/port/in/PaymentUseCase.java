package com.school.billing.application.port.in;

import com.school.billing.domain.model.Payment;
import java.util.List;

public interface PaymentUseCase {
    List<Payment> list(PaymentFilter filter);
    Payment get(Long id);
    Payment create(Payment payment);
    Payment update(Long id, Payment payment);
    void delete(Long id);
    PaymentSummary summarize();
    byte[] generateReceipt(Long id);
}

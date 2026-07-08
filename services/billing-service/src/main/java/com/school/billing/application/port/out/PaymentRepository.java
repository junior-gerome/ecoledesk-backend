package com.school.billing.application.port.out;

import com.school.billing.application.port.in.PaymentFilter;
import com.school.billing.domain.model.Payment;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    List<Payment> findAll(PaymentFilter filter);
    Optional<Payment> findById(Long id);
    Payment save(Payment payment);
    void deleteById(Long id);
}

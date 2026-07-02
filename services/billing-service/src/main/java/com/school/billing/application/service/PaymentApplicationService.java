package com.school.billing.application.service;

import com.school.billing.application.port.in.PaymentFilter;
import com.school.billing.application.port.in.PaymentSummary;
import com.school.billing.application.port.in.PaymentUseCase;
import com.school.billing.application.port.out.PaymentRepository;
import com.school.billing.domain.model.Payment;
import com.school.billing.domain.model.PaymentStatus;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentApplicationService implements PaymentUseCase {
    private final PaymentRepository repository;

    public PaymentApplicationService(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Payment> list(PaymentFilter filter) {
        return repository.findAll(filter).stream()
                .map(payment -> payment.markLateIfOverdue(LocalDate.now()))
                .toList();
    }

    @Override
    public Payment get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    public Payment create(Payment payment) {
        return repository.save(payment.markLateIfOverdue(LocalDate.now()));
    }

    @Override
    public Payment update(Long id, Payment payment) {
        get(id);
        Payment next = new Payment(id, payment.studentId(), payment.studentName(), payment.amount(),
                payment.paymentDate(), payment.dueDate(), payment.type(), payment.status(),
                payment.paymentMethod(), payment.receiptNumber(), payment.description(),
                payment.createdAt(), payment.updatedAt());
        return repository.save(next.markLateIfOverdue(LocalDate.now()));
    }

    @Override
    public void delete(Long id) {
        get(id);
        repository.deleteById(id);
    }

    @Override
    public PaymentSummary summarize() {
        List<Payment> payments = list(new PaymentFilter(null, null, null, null, null));
        BigDecimal total = sum(payments);
        BigDecimal paid = sumByStatus(payments, PaymentStatus.PAID);
        BigDecimal pending = sumByStatus(payments, PaymentStatus.PENDING);
        BigDecimal late = sumByStatus(payments, PaymentStatus.LATE);
        return new PaymentSummary(total, paid, pending, late);
    }

    @Override
    public byte[] generateReceipt(Long id) {
        Payment payment = get(id);
        String receipt = """
                SCHOOL PAYMENT RECEIPT
                Receipt: %s
                Student: %s
                Amount: %s
                Type: %s
                Status: %s
                Date: %s
                """.formatted(
                payment.receiptNumber() == null ? "N/A" : payment.receiptNumber(),
                payment.studentName() == null ? payment.studentId() : payment.studentName(),
                payment.amount().amount(),
                payment.type(),
                payment.status(),
                payment.paymentDate());
        return receipt.getBytes(StandardCharsets.UTF_8);
    }

    private BigDecimal sum(List<Payment> payments) {
        return payments.stream()
                .map(payment -> payment.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByStatus(List<Payment> payments, PaymentStatus status) {
        return payments.stream()
                .filter(payment -> payment.status() == status)
                .map(payment -> payment.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

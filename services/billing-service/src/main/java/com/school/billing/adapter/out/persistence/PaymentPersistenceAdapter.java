package com.school.billing.adapter.out.persistence;

import com.school.billing.application.port.in.PaymentFilter;
import com.school.billing.application.port.out.PaymentRepository;
import com.school.billing.domain.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class PaymentPersistenceAdapter implements PaymentRepository {
    private final PaymentJpaRepository repository;

    PaymentPersistenceAdapter(PaymentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Payment> findAll(PaymentFilter filter) {
        return repository.findAll(toSpecification(filter)).stream()
                .map(PaymentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return repository.findById(id).map(PaymentPersistenceMapper::toDomain);
    }

    @Override
    public Payment save(Payment payment) {
        return PaymentPersistenceMapper.toDomain(repository.save(PaymentPersistenceMapper.toEntity(payment)));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private Specification<PaymentJpaEntity> toSpecification(PaymentFilter filter) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (filter == null) {
                return predicate;
            }
            if (filter.studentId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("studentId"), filter.studentId()));
            }
            if (filter.status() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), filter.status()));
            }
            if (filter.type() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("type"), filter.type()));
            }
            if (filter.startDate() != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("paymentDate"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("paymentDate"), filter.endDate()));
            }
            return predicate;
        };
    }
}

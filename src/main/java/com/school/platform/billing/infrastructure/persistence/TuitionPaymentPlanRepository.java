package com.school.platform.billing.infrastructure.persistence;

import com.school.platform.billing.domain.model.TuitionPaymentPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TuitionPaymentPlanRepository extends JpaRepository<TuitionPaymentPlan, Long> {

    boolean existsByEnrollmentId(Long enrollmentId);

    Optional<TuitionPaymentPlan> findByEnrollmentId(Long enrollmentId);
}
package com.school.platform.billing.infrastructure.persistence;

import com.school.platform.billing.domain.model.PreEnrollmentFeePayment;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreEnrollmentFeePaymentRepository extends JpaRepository<PreEnrollmentFeePayment, Long> {

    boolean existsByTransactionReference(String transactionReference);

    @Query("""
            select coalesce(sum(payment.amount), 0)
            from PreEnrollmentFeePayment payment
            where payment.preEnrollment.id = :preEnrollmentId
              and payment.verified = true
            """)
    BigDecimal sumVerifiedAmountByPreEnrollmentId(@Param("preEnrollmentId") Long preEnrollmentId);
}
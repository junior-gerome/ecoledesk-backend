package com.school.platform.billing.domain.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.billing.domain.model.Montant;
import com.school.platform.enrollment.domain.model.Student;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"student", "enrollment", "montant"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "paiements")
public class Paiement {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(name = "montant_paye")
    private BigDecimal montantPaye;

    @Column(name = "montant_restant")
    private BigDecimal montantRestant;

    @Column(name = "remise")
    private BigDecimal remise;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne
    @JoinColumn(name = "montant_id", nullable = false)
    private Montant montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)
    private TypePaiement typePaiement;

    @Version
    private Long version;

    public boolean isCancelled() {
        return cancelledAt != null;
    }
}
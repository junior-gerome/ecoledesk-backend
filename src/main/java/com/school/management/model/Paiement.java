package com.school.management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.school.management.enums.TypePaiement;

@Data
@Entity
@Table(name = "paiements")
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type_paiement", nullable = false)
    private TypePaiement typePaiement;
}

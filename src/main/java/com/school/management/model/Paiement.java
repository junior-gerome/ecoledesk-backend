package com.school.management.model;

import jakarta.persistence.*;
import lombok.Data;
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

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Column(name = "montant_paye")
    private Long montantPaye;

    @Column(name = "montant_restant")
    private Long montanRestant;

    @Column(name = "remise")
    private Long remise;
    
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "inscriptionStudent_id", nullable = false)
    private InscriptionStudent inscriptionStudent;

    @ManyToOne
    @JoinColumn(name = "montant_id", nullable = false)
    private Montant montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type_paiement", nullable = false)
    private TypePaiement typePaiement;
}

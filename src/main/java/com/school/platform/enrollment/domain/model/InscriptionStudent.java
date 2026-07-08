package com.school.platform.enrollment.domain.model;

import com.school.platform.billing.domain.model.Montant;

import com.school.platform.academic.domain.model.ClasseRoom;

import com.school.platform.academic.domain.model.AnneeScolaire;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"student", "classeRoom", "montant", "anneeScolaire"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "inscription_student")
public class InscriptionStudent {
    
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name ="student_id",nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "classe_room_id", nullable = false)
    private ClasseRoom classeRoom;

    @ManyToOne
    @JoinColumn(name = "montant_id", nullable = false)
    private Montant montant;

    @ManyToOne
    @JoinColumn(name ="anneescolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;

    @Column(name = "date_inscription", nullable = false)
    private LocalDate DateInscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_preinscription", nullable = false, length = 30)
    private PreinscriptionStatus statutPreinscription = PreinscriptionStatus.VALIDEE;

    @Column(name = "date_preinscription")
    private LocalDate datePreinscription;

    @Column(name = "preinscription_decision_reason", length = 500)
    private String preinscriptionDecisionReason;

    @Column(name = "preinscription_decision_by")
    private Long preinscriptionDecisionBy;

    @Column(name = "preinscription_decision_at")
    private LocalDateTime preinscriptionDecisionAt;

    @Version
    private Long version;

    @PrePersist
    void applyDefaults() {
        if (statutPreinscription == null) {
            statutPreinscription = PreinscriptionStatus.VALIDEE;
        }
        if (datePreinscription == null) {
            datePreinscription = DateInscription;
        }
    }
    
}

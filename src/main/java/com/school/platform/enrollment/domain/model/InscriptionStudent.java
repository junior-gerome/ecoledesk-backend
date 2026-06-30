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

    @Column(name = "statut_preinscription", nullable = false, length = 30)
    private String statutPreinscription = "INSCRITE";

    @Column(name = "date_preinscription")
    private LocalDate datePreinscription;

    @Version
    private Long version;

    @PrePersist
    void applyDefaults() {
        if (statutPreinscription == null || statutPreinscription.isBlank()) {
            statutPreinscription = "INSCRITE";
        }
        if (datePreinscription == null) {
            datePreinscription = DateInscription;
        }
    }
    
}

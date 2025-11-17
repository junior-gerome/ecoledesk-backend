package com.school.management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "inscriptionStudent")
public class InscriptionStudent {
    
    @Id
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

    
}

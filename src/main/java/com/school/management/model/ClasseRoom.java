package com.school.management.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "classes")
public class ClasseRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nameClasse;

    @Column(name = "niveau", nullable = false, length = 50)
    private String level;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(name = "capacite")
    private Integer capacity;

     @ManyToOne
    @JoinColumn(name = "teacher_id")  // colonne fk
    private Teacher teacher;    

    @ManyToOne
    @JoinColumn(name = "anneescolaire_id")
    private AnneeScolaire anneeScolaire;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}

package com.school.management.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Autres champs, getters, setters...
}


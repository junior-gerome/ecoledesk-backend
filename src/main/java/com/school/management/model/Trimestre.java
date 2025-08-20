package com.school.management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.school.management.enums.TypeTrimestre;
import com.school.management.model.converter.TrimestreTypeConverter;

@Data
@Entity
@Table(name = "trimestre")
public class Trimestre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String libelleTrimestre;

    @Column(nullable = false)
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annee_scolaire_id", nullable = false)
    private AnneeScolaire anneeScolaire;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Convert(converter = TrimestreTypeConverter.class)
    @Column(name = "type", nullable = false, length = 20)
    private TypeTrimestre type;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

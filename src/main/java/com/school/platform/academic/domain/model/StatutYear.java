package com.school.platform.academic.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "statut_year")
public class StatutYear {
    @Id
    @Column(name = "code", length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;
}

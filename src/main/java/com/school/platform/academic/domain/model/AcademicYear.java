package com.school.platform.academic.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

import com.school.platform.identityaccess.domain.model.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "academic_year")
public class AcademicYear extends BaseEntity {
    

    @Column(nullable = false, length = 50)
    private String libelleAcademicYear;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "statut_code", nullable = false, length = 20)
    private boolean statutCode;

}

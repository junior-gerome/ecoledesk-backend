package com.school.platform.academic.application.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AffectationCreateRequest {
    @NotBlank(message = "Le type d'affectation est obligatoire")
    private String typeCode;

    @NotNull(message = "L'enseignant est obligatoire")
    @Positive(message = "L'identifiant de l'enseignant doit etre positif")
    private Long teacherId;

    @NotNull(message = "La classe est obligatoire")
    @Positive(message = "L'identifiant de la classe doit etre positif")
    private Long classeId;

    @NotNull(message = "La matiere est obligatoire")
    @Positive(message = "L'identifiant de la matiere doit etre positif")
    private Long subjectId;

    @NotNull(message = "L'annee scolaire est obligatoire")
    @Positive(message = "L'identifiant de l'annee scolaire doit etre positif")
    private Long academicYearId;

    private LocalDate dateDebut;
    private LocalDate dateFin;
}

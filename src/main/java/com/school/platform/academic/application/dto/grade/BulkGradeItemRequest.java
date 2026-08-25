package com.school.platform.academic.application.dto.grade;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BulkGradeItemRequest {
    @NotNull(message = "L'eleve est obligatoire")
    @Positive(message = "L'identifiant de l'eleve doit etre positif")
    private Long studentId;

    @NotNull(message = "La note est obligatoire")
    @DecimalMin(value = "0.00", message = "La note ne peut pas etre negative")
    @DecimalMax(value = "20.00", message = "La note ne peut pas depasser 20")
    private BigDecimal score;

    @DecimalMin(value = "0.1", message = "Le coefficient doit etre au moins 0.1")
    private BigDecimal coefficient;

    @Size(max = 1000, message = "Le commentaire ne doit pas depasser 1000 caracteres")
    private String comments;
}

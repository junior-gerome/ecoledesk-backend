package com.school.platform.academic.application.dto.grade;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GradeBatchStatusRequest {
    @NotNull(message = "La classe est obligatoire")
    @Positive(message = "L'identifiant de la classe doit etre positif")
    private Long classeId;

    @Positive(message = "L'identifiant de la matiere doit etre positif")
    private Long subjectId;

    @Positive(message = "L'identifiant de la sequence doit etre positif")
    private Long sequenceId;

    private String period;

    @Size(max = 500, message = "La justification ne doit pas depasser 500 caracteres")
    private String justification;
}

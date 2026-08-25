package com.school.platform.academic.application.dto.grade;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BulkGradeCreateRequest {
    @NotNull(message = "La classe est obligatoire")
    @Positive(message = "L'identifiant de la classe doit etre positif")
    private Long classeId;

    @NotNull(message = "La matiere est obligatoire")
    @Positive(message = "L'identifiant de la matiere doit etre positif")
    private Long subjectId;

    @NotNull(message = "La sequence est obligatoire")
    @Positive(message = "L'identifiant de la sequence doit etre positif")
    private Long sequenceId;

    private Long trimestreId;
    private String period;
    private LocalDateTime assessmentDate;

    @Valid
    @NotEmpty(message = "Au moins une note est obligatoire")
    private List<BulkGradeItemRequest> grades;
}

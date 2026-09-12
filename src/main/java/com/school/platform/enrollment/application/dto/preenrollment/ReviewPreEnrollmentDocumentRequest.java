package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Corps de la revue d'une piece justificative.
 * L'identite du revieur est toujours extraite du contexte de securite cote serveur.
 */
@Data
public class ReviewPreEnrollmentDocumentRequest {

    @NotNull(message = "Le statut de revue est obligatoire")
    private DocumentReviewStatus status;

    @Size(max = 500, message = "Le motif ne doit pas depasser 500 caracteres")
    private String reason;
}
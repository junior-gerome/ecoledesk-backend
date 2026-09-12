package com.school.platform.enrollment.application.dto.preenrollment;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Corps des decisions administratives (approbation, rejet, demarrage de revue).
 * L'identite de l'agent est toujours extraite du contexte de securite cote serveur
 * et n'est jamais fournie par le client.
 */
@Data
public class DecisionRequest {

    @Size(max = 500, message = "Le motif ne doit pas depasser 500 caracteres")
    private String reason;
}
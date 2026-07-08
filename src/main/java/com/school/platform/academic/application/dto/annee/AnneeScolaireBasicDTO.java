package com.school.platform.academic.application.dto.annee;

import lombok.Data;

/**
 * DTO basique année scolaire : id + libellé + statut.
 */
@Data
public class AnneeScolaireBasicDTO {
    private Long id;
    private String libelleAnneeScolaire;
    private boolean statutCode;
}

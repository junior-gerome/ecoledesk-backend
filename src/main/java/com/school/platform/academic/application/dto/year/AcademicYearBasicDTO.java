package com.school.platform.academic.application.dto.year;

import lombok.Data;

/**
 * DTO basique année scolaire : id + libellé + statut.
 */
@Data
public class AcademicYearBasicDTO {
    private Long id;
    private String libelleAcademicYear;
    private boolean statutCode;
}

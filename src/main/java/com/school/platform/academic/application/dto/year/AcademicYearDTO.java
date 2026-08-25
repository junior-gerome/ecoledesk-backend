package com.school.platform.academic.application.dto.year;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AcademicYearDTO {
    private Long id;
    private String libelleAcademicYear;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean statutCode;
}

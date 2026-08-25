package com.school.platform.academic.application.dto.affectation;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AffectationResponse {
    private Long id;
    private String typeCode;
    private Long teacherId;
    private Long classeId;
    private Long subjectId;
    private Long academicYearId;
    private String teacherName;
    private String classeName;
    private String subjectName;
    private String libelleAcademicYear;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}

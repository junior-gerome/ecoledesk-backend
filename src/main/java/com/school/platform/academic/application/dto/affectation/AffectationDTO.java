package com.school.platform.academic.application.dto.affectation;

import lombok.*;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AffectationDTO {
    private Long id;
    private String typeCode;
    private Long teachertId;
    private Long classeId;
    private Long subjectId;
    private Long academicYearId;
    private String LastnameTeacher;
    private String NameClasse;
    private String nameSubject;
    private String libelleAcademicYear;
    private LocalDate dateDebut;
    private LocalDate dateFin;
   

}

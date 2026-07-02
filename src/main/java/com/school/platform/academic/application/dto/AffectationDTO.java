package com.school.platform.academic.application.dto;

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
    private Long anneeScolaireId;
    private String LastnameTeacher;
    private String NameClasse;
    private String nameSubject;
    private String libelleAnneeScolaire;
    private LocalDate dateDebut;
    private LocalDate dateFin;
   

}

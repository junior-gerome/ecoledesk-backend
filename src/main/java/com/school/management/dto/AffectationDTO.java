package com.school.management.dto;

import lombok.*;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
public class AffectationDTO {
    private Long id;
    private String typeCode;
    private Long teachertId;
    private String LastnameTeacher;
    private String NameClasse;
    private String nameSubject;
    private String libelleAnneeScolaire;
    private LocalDate dateDebut;
    private LocalDate dateFin;
   

}

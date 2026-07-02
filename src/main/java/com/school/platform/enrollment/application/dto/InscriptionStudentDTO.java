package com.school.platform.enrollment.application.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class InscriptionStudentDTO {
    private Long id;
    private StudentDTO student;
    private Long classeRoomId;
    private Long sectionId;
    private Long montantId;
    private Long anneeScolaireId;
    private LocalDate dateInscription;
    private String statutPreinscription;
    private LocalDate datePreinscription;
}

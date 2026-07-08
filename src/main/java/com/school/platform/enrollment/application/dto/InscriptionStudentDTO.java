package com.school.platform.enrollment.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.school.platform.enrollment.domain.model.PreinscriptionStatus;

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
    private PreinscriptionStatus statutPreinscription;
    private LocalDate datePreinscription;
    private String preinscriptionDecisionReason;
    private Long preinscriptionDecisionBy;
    private LocalDateTime preinscriptionDecisionAt;
}

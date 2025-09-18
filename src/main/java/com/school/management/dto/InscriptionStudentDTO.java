package com.school.management.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class InscriptionStudentDTO {
    private Long id;
    private Long studentId;
    private Long classeRoomId;
    private Long montantId;
    private Long anneeScolaireId;
    private LocalDate dateInscription;
}

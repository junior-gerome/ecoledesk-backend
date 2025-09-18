package com.school.management.dto;

import lombok.Data;

@Data
public class InscriptionRequest {
    private StudentDTO student;
    private Long classeRoomId;
    private Long montantId;
    private Long anneeScolaireId;
}
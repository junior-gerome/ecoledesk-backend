package com.school.platform.academic.application.dto.section;

import lombok.Data;

/**
 * DTO basique section : id + libellé.
 */
@Data
public class SectionBasicDTO {
    private Long id;
    private String libelle;
}

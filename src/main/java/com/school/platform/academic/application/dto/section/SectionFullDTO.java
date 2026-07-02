package com.school.platform.academic.application.dto.section;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO complet section : inclut la description.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SectionFullDTO extends SectionBasicDTO {

    @NotBlank(message = "Le libelle de la section est obligatoire")
    private String libelle;

    private String description;
}

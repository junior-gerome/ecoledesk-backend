package com.school.platform.academic.application.dto.teacher;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO complet enseignant : inclut date d'embauche, CNI et photo.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherFullDTO extends TeacherMediumDTO {

    private LocalDate dateEmbauche;
    private String cniNumber;
    private String cniPhotoUrl;
}

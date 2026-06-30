package com.school.platform.academic.application.dto.teacher;

import lombok.Data;

/**
 * DTO basique enseignant : identifiant + nom + prénom pour les listes/dropdowns.
 */
@Data
public class TeacherBasicDTO {
    private Long id;
    private String lastnameTeacher;
    private String firstnameTeacher;
    private String photoUrl;
}

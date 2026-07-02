package com.school.platform.academic.application.dto.teacher;

import com.school.platform.enrollment.domain.model.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO medium enseignant : champs principaux sans données sensibles.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherMediumDTO extends TeacherBasicDTO {

    @NotBlank(message = "Le nom est obligatoire")
    private String lastnameTeacher;

    @NotBlank(message = "Le prenom est obligatoire")
    private String firstnameTeacher;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit etre valide")
    private String email;

    @NotBlank(message = "Le telephone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numero de telephone doit etre valide")
    private String phoneNumber;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    private String niveau;
    private String speciality;
    private String adress;
}

package com.school.platform.enrollment.application.dto;

import java.time.LocalDate;

import com.school.platform.enrollment.domain.model.Gender;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentDTO {
    private Long id;

    @NotBlank(message = "Le prenom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prenom doit contenir entre 2 et 100 caracteres")
    private String firstNameStudent;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caracteres")
    private String lastNameStudent;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit etre dans le passe")
    private LocalDate dateOfBirth;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    @Size(max = 100, message = "L'ecole precedente ne doit pas depasser 100 caracteres")
    private String ecolePrecedente;

    private String photoUrl;

    @Valid
    @NotNull(message = "Le responsable legal est obligatoire")
    private GuardianDTO guardian;
}

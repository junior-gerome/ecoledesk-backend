package com.school.platform.enrollment.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Donnees d'un responsable legal. Les informations d'identite appartiennent
 * a la {@code Person} associee; ce DTO les expose uniquement pour l'API.
 */
@Data
public class GuardianDTO {
    private Long id;

    @NotBlank(message = "Le nom du responsable legal est obligatoire")
    @Size(max = 100)
    private String lastNameGuardian;

    @NotBlank(message = "Le prenom du responsable legal est obligatoire")
    @Size(max = 100)
    private String firstNameGuardian;

    @Email(message = "L'email doit etre valide")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Le telephone du responsable legal est obligatoire")
    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 255)
    private String address;

    @Size(max = 100)
    private String occupation;
}

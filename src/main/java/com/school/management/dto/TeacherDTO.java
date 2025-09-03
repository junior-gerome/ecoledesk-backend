package com.school.management.dto;

import java.time.LocalDate;
import com.school.management.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDTO {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastnameTeacher;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstnameTeacher;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide")
    private String phoneNumber;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    private String niveau;
    private String speciality;
    private String adress;
    private LocalDate dateEmbauche;
}

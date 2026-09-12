package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.model.RelationshipType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddPreEnrollmentGuardianRequest {

    @NotNull(message = "Le type de relation est obligatoire")
    private RelationshipType relationshipType;

    @Size(max = 255, message = "Le detail de la relation ne doit pas depasser 255 caracteres")
    private String relationshipDetails;

    @NotBlank(message = "Le prénom du responsable est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom du responsable doit contenir entre 2 et 100 caracteres")
    private String firstName;

    @NotBlank(message = "Le nom du responsable est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom du responsable doit contenir entre 2 et 100 caracteres")
    private String lastName;

    @Email(message = "L'adresse email du responsable est invalide")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s().-]{8,20}$",
            message = "Le numero de telephone du responsable est invalide (8 a 15 chiffres attendus)")
    private String phoneNumber;

    @Size(max = 255, message = "L'adresse du responsable ne doit pas depasser 255 caracteres")
    private String address;

    private boolean primaryContact;

    private boolean financialResponsible;

    private boolean emergencyContact;
}

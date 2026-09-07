package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.model.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPreEnrollmentGuardianRequest {

    @NotNull(message = "Le type de relation est obligatoire")
    private RelationshipType relationshipType;

    private String relationshipDetails;

    @NotBlank(message = "Le prénom du responsable est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom du responsable est obligatoire")
    private String lastName;

    private String email;

    private String phoneNumber;

    private String address;

    private boolean primaryContact;

    private boolean financialResponsible;

    private boolean emergencyContact;
}

package com.school.platform.enrollment.application.dto.preenrollment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddPreEnrollmentDocumentRequest {

    @NotBlank(message = "Le type de document est obligatoire")
    private String documentType;

    @NotBlank(message = "La référence de stockage est obligatoire")
    private String storageReference;
}
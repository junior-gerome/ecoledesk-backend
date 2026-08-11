package com.school.platform.enrollment.application.dto.preenrollment;

import lombok.Data;

@Data
public class AddPreEnrollmentDocumentRequest {
    private String documentType;
    private String storageReference;
}
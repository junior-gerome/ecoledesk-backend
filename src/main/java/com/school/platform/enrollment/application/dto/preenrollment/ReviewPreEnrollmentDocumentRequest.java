package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewPreEnrollmentDocumentRequest {
    private Long reviewedBy;

    @NotNull(message = "Le statut de revue est obligatoire")
    private DocumentReviewStatus status;

    private String reason;
}
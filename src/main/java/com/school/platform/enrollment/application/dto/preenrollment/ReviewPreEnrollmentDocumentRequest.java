package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;
import lombok.Data;

@Data
public class ReviewPreEnrollmentDocumentRequest {
    private Long reviewedBy;
    private DocumentReviewStatus status;
    private String reason;
}
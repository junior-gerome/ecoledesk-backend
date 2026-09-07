package com.school.platform.enrollment.domain.policy;

import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "school.enrollment")
@Getter
@Setter
public class RequiredPreEnrollmentDocumentPolicy {

    private List<String> requiredDocumentTypes = List.of();

    public boolean mandatoryDocumentsAreSubmitted(PreEnrollment preEnrollment) {
        if (requiredDocumentTypes == null || requiredDocumentTypes.isEmpty()) {
            return true;
        }
        return requiredDocumentTypes.stream()
                .filter(type -> type != null && !type.isBlank())
                .allMatch(requiredType -> preEnrollment.getDocuments().stream()
                        .anyMatch(document -> requiredType.trim().equalsIgnoreCase(document.getDocumentType())
                                && document.getReviewStatus() != null));
    }

    public boolean mandatoryDocumentsAreApproved(PreEnrollment preEnrollment) {
        boolean hasInvalidDocuments = preEnrollment.getDocuments().stream()
                .anyMatch(d -> d.getReviewStatus() == DocumentReviewStatus.REJECTED
                        || d.getReviewStatus() == DocumentReviewStatus.REPLACEMENT_REQUIRED);
        if (hasInvalidDocuments) {
            return false;
        }

        if (requiredDocumentTypes == null || requiredDocumentTypes.isEmpty()) {
            return true;
        }

        return requiredDocumentTypes.stream()
                .filter(type -> type != null && !type.isBlank())
                .allMatch(requiredType -> preEnrollment.getDocuments().stream()
                        .anyMatch(document -> requiredType.trim().equalsIgnoreCase(document.getDocumentType())
                                && document.getReviewStatus() == DocumentReviewStatus.APPROVED));
    }
}
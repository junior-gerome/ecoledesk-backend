package com.school.platform.enrollment.domain.preenrollment;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pre_enrollment_documents")
@Getter
@NoArgsConstructor
public class PreEnrollmentDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_enrollment_id", nullable = false)
    private PreEnrollment preEnrollment;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "storage_reference", nullable = false, length = 500)
    private String storageReference;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 30)
    private DocumentReviewStatus reviewStatus = DocumentReviewStatus.SUBMITTED;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public static PreEnrollmentDocument submitted(
            PreEnrollment preEnrollment, String documentType, String storageReference) {
        if (documentType == null || documentType.isBlank() || storageReference == null || storageReference.isBlank()) {
            throw new IllegalArgumentException("Document type and storage reference are required");
        }
        PreEnrollmentDocument document = new PreEnrollmentDocument();
        document.preEnrollment = preEnrollment;
        document.documentType = documentType.trim();
        document.storageReference = storageReference.trim();
        document.submittedAt = LocalDateTime.now();
        return document;
    }

    public void review(DocumentReviewStatus status, Long userId, String reason) {
        if (status != DocumentReviewStatus.APPROVED
                && status != DocumentReviewStatus.REJECTED
                && status != DocumentReviewStatus.REPLACEMENT_REQUIRED) {
            throw new IllegalArgumentException("A document review must approve, reject or request replacement");
        }
        if (status != DocumentReviewStatus.APPROVED && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("A reason is required when a document is not approved");
        }
        reviewStatus = status;
        rejectionReason = status == DocumentReviewStatus.APPROVED ? null : reason.trim();
        reviewedBy = userId;
        reviewedAt = LocalDateTime.now();
    }
}
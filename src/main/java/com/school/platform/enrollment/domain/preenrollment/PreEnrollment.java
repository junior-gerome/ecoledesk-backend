package com.school.platform.enrollment.domain.preenrollment;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import com.school.platform.academic.domain.model.AcademicYear;
import jakarta.persistence.*;
import java.math.BigDecimal; 
import java.time.LocalDateTime; 
import java.util.*; import lombok.*;

@Entity 
@Table(name = "pre_enrollments")
@Getter @NoArgsConstructor
public class PreEnrollment extends BaseEntity {
  
 @Embedded @AttributeOverride(name="value", column=@Column(name="number", nullable=false, unique=true, length=50)) 
 private PreEnrollmentNumber number;
 
 @Embedded private ApplicantIdentity applicant = new ApplicantIdentity();
 @ManyToOne(fetch=FetchType.LAZY) 
 @JoinColumn(name = "academic_year_id", nullable = false)
 private AcademicYear academicYear;

 @Column(name = "requested_level", nullable = false, length = 100)
 private String requestedLevel;

 @Enumerated(EnumType.STRING) 
 @Column(nullable=false,length=30) 
 private PreEnrollmentStatus status = PreEnrollmentStatus.DRAFT;
 
 @Column(name="required_fee",precision=12,scale=2)
 private BigDecimal requiredFee;
     
 @Column(name="fee_payment_reference",length=100) 
 private String feePaymentReference;

 @Column(name="submitted_at") 
 private LocalDateTime submittedAt;

 @Column(name = "reviewed_at")
 private LocalDateTime reviewedAt;
 
 @Column(name = "reviewed_by")
 private Long reviewedBy;

 @Column(name = "rejection_reason", length = 500)
 private String rejectionReason;
 
 @Column(name="administrative_comment",length=1000) 
 private String administrativeComment;
        
 @OneToMany(mappedBy="preEnrollment",cascade=CascadeType.ALL,orphanRemoval=true) 
 private final Set<PreEnrollmentGuardian> guardians=new HashSet<>();
 
 @OneToMany(mappedBy = "preEnrollment", cascade = CascadeType.ALL, orphanRemoval = true)
 private final Set<PreEnrollmentDocument> documents=new HashSet<>();
 
 public static PreEnrollment draft(PreEnrollmentNumber number, ApplicantIdentity applicant, AcademicYear academicYear,
     String requestedLevel, BigDecimal requiredFee) {
   PreEnrollment pre = new PreEnrollment();
   pre.number = number;
   pre.applicant = applicant;
   pre.academicYear = academicYear;
   pre.requestedLevel = requestedLevel;
   pre.requiredFee = requiredFee;
   return pre;
 }
 
 public void submit(boolean feeConfirmed, boolean mandatoryDocumentsApproved) {
   if (status != PreEnrollmentStatus.DRAFT)
     throw new IllegalStateException("Only a draft can be submitted");
   if (applicant == null || !applicant.isComplete() || academicYear == null || requestedLevel == null
       || requestedLevel.isBlank())
     throw new IllegalStateException("Incomplete applicant file");
   if (guardians.stream().noneMatch(PreEnrollmentGuardian::isPrimaryContactable))
      throw new IllegalStateException("A contactable primary guardian is required"); 
    if (!feeConfirmed || !mandatoryDocumentsApproved)
      throw new IllegalStateException("Fee and mandatory documents must be confirmed");
    status = PreEnrollmentStatus.SUBMITTED;
    submittedAt = LocalDateTime.now();
  }

  public void startReview(Long userId) {
    if (status != PreEnrollmentStatus.SUBMITTED)
      throw new IllegalStateException("Only submitted files can be reviewed");
    status = PreEnrollmentStatus.UNDER_REVIEW;
    reviewedBy = userId;
    reviewedAt = LocalDateTime.now();
  }
  
  public void approve(Long userId) {
  if(status!=PreEnrollmentStatus.UNDER_REVIEW) throw new IllegalStateException("Only reviewed files can be approved");
  if (guardians.stream().noneMatch(PreEnrollmentGuardian::isPrimaryContactable))
    throw new IllegalStateException("A contactable primary guardian is required"); status=PreEnrollmentStatus.APPROVED; reviewedBy=userId; 
   reviewedAt = LocalDateTime.now();
  }

  public void reject(Long userId, String reason) {
    decide(userId, reason, PreEnrollmentStatus.REJECTED);
 }

 public void cancel(String reason) {
   if (reason == null || reason.isBlank())
     throw new IllegalArgumentException("Cancellation reason is required");
   if (status == PreEnrollmentStatus.APPROVED)
     throw new IllegalStateException("An approved file cannot be cancelled");
   status = PreEnrollmentStatus.CANCELLED;
   rejectionReason = reason.trim();
 }
  
 private void decide(Long userId,String reason,PreEnrollmentStatus target){ 
  if(status!=PreEnrollmentStatus.SUBMITTED&&status!=PreEnrollmentStatus.UNDER_REVIEW)
    throw new IllegalStateException("Decision is not allowed");
  if (reason == null || reason.isBlank())
      throw new IllegalArgumentException("Reason is required"); 
    status = target;
    rejectionReason=reason.trim();
    reviewedBy=userId; reviewedAt=LocalDateTime.now();
 }

/**
 * Ajoute un document administratif au dossier de préinscription.
 */
public void addDocument(PreEnrollmentDocument document) {
    if (document == null) {
        throw new IllegalArgumentException(
                "Pre-enrollment document cannot be null"
        );
    }

    if (status != PreEnrollmentStatus.DRAFT) {
        throw new IllegalStateException(
                "Documents can only be added to a draft pre-enrollment"
        );
    }

    if (document.getPreEnrollment() != this) {
        throw new IllegalArgumentException(
                "The document does not belong to this pre-enrollment"
        );
    }

    documents.add(document);
}

/**
 * Vérifie un document appartenant à cette préinscription.
 */
public void reviewDocument(
        Long documentId,
        DocumentReviewStatus status,
        Long reviewedBy,
        String reason
) {
    if (documentId == null) {
        throw new IllegalArgumentException(
                "Document id is required"
        );
    }

    if (status == null) {
        throw new IllegalArgumentException(
                "Document review status is required"
        );
    }

    if (reviewedBy == null) {
        throw new IllegalArgumentException(
                "Reviewer id is required"
        );
    }

    if (this.status != PreEnrollmentStatus.UNDER_REVIEW) {
        throw new IllegalStateException(
                "Documents can only be reviewed when the pre-enrollment is under review"
        );
    }

    PreEnrollmentDocument document = documents.stream()
            .filter(currentDocument ->
                    documentId.equals(currentDocument.getId())
            )
            .findFirst()
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Document not found in pre-enrollment: "
                                    + documentId
                    )
            );

    document.review(status, reviewedBy, reason);
}

}

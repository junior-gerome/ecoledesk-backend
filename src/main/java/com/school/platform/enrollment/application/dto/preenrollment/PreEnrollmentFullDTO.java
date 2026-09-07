package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.model.RelationshipType;
import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreEnrollmentFullDTO {
    private Long id;
    private String number;
    private PreEnrollmentStatus status;
    private String applicantFirstName;
    private String applicantLastName;
    private LocalDate applicantBirthDate;
    private Gender applicantGender;
    private String applicantBirthPlace;
    private String requestedLevel;
    private Long academicYearId;
    private String academicYearLabel;
    private BigDecimal requiredFee;
    private String feePaymentReference;
    private String rejectionReason;
    private String administrativeComment;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private List<PreEnrollmentGuardianDTO> guardians;
    private List<PreEnrollmentDocumentDTO> documents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreEnrollmentGuardianDTO {
        private Long id;
        private RelationshipType relationshipType;
        private String relationshipDetails;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String address;
        private boolean primaryContact;
        private boolean financialResponsible;
        private boolean emergencyContact;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreEnrollmentDocumentDTO {
        private Long id;
        private String documentType;
        private String storageReference;
        private DocumentReviewStatus reviewStatus;
        private String rejectionReason;
        private LocalDateTime submittedAt;
        private LocalDateTime reviewedAt;
    }
}

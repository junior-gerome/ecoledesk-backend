package com.school.platform.enrollment.application.preenrollment;



import java.time.Year;
import java.util.UUID;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.enrollment.domain.preenrollment.*;
import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.enrollment.application.dto.preenrollment.*;
import com.school.platform.enrollment.application.mapper.PreEnrollmentMapper;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.enrollment.domain.policy.RequiredPreEnrollmentDocumentPolicy;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.billing.infrastructure.persistence.PreEnrollmentFeePaymentRepository;




@Service
@RequiredArgsConstructor
public class PreEnrollmentCommandServiceImpl implements PreEnrollmentCommandService {

    private final PreEnrollmentRepository preEnrollmentRepository;
    private final AcademicYearRepository yearRepository;
    private final PreEnrollmentFeePaymentRepository feePaymentRepository;
    private final RequiredPreEnrollmentDocumentPolicy documentPolicy;
    private final PreEnrollmentMapper preEnrollmentMapper;

    @Override
    @Transactional
    public PreEnrollmentResponse createDraft(CreatePreEnrollmentRequest request) {
        AcademicYear year = yearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academicyear", "id", request.getAcademicYearId()));

        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName(request.getFirstName());
        applicant.setLastName(request.getLastName());
        applicant.setBirthDate(request.getBirthDate());
        applicant.setGender(request.getGender());
        applicant.setBirthPlace(request.getBirthPlace());

        String number = "PRE-" + Year.now().getValue() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PreEnrollment preEnrollment = PreEnrollment.draft(
                PreEnrollmentNumber.of(number), applicant, year, request.getRequestedLevel(), request.getRequiredFee());
        return response(preEnrollmentRepository.save(preEnrollment));
    }

    @Override
    @Transactional
    public PreEnrollmentResponse addGuardian(Long id, AddPreEnrollmentGuardianRequest request) {
        PreEnrollment preEnrollment = find(id);
        if (preEnrollment.getStatus() != PreEnrollmentStatus.DRAFT) {
            throw new IllegalStateException("Guardians can only be changed on a draft");
        }

        PreEnrollmentGuardian guardian = new PreEnrollmentGuardian();
        guardian.setPreEnrollment(preEnrollment);
        guardian.setRelationshipType(request.getRelationshipType());
        guardian.setRelationshipDetails(request.getRelationshipDetails());
        guardian.setFirstName(request.getFirstName());
        guardian.setLastName(request.getLastName());
        guardian.setEmail(request.getEmail());
        guardian.setPhoneNumber(request.getPhoneNumber());
        guardian.setAddress(request.getAddress());
        guardian.setPrimaryContact(request.isPrimaryContact());
        guardian.setFinancialResponsible(request.isFinancialResponsible());
        guardian.setEmergencyContact(request.isEmergencyContact());
        preEnrollment.getGuardians().add(guardian);
        return response(preEnrollmentRepository.save(preEnrollment));
    }

    @Override
@Transactional
public PreEnrollmentResponse addDocument(
        Long preEnrollmentId,
        AddPreEnrollmentDocumentRequest request
) {
    PreEnrollment preEnrollment = find(preEnrollmentId);

    PreEnrollmentDocument document =
            preEnrollmentMapper.toDocument(
                    request,
                    preEnrollment
            );

    preEnrollment.addDocument(document);

    PreEnrollment saved =
            preEnrollmentRepository.save(preEnrollment);

    return preEnrollmentMapper.toResponse(saved);
}

@Override
@Transactional
public PreEnrollmentResponse reviewDocument(
        Long preEnrollmentId,
        Long documentId,
        DocumentReviewStatus status,
        Long reviewedBy,
        String reason
) {
    PreEnrollment preEnrollment = find(preEnrollmentId);

    preEnrollment.reviewDocument(
            documentId,
            status,
            reviewedBy,
            reason
    );

    PreEnrollment saved =
            preEnrollmentRepository.save(preEnrollment);

    return preEnrollmentMapper.toResponse(saved);
}

    @Override
    @Transactional
    public PreEnrollmentResponse submit(Long id) {
        PreEnrollment preEnrollment = find(id);
        preEnrollment.submit(hasRequiredFeePayment(preEnrollment), documentPolicy.mandatoryDocumentsAreApproved(preEnrollment));
        return response(preEnrollmentRepository.save(preEnrollment));
    }

    @Override
    @Transactional
    public PreEnrollmentResponse startReview(Long id, Long reviewedBy) {
        PreEnrollment preEnrollment = find(id);
        preEnrollment.startReview(reviewedBy);
        return response(preEnrollmentRepository.save(preEnrollment));
    }

    @Override
    @Transactional
    public PreEnrollmentResponse approve(Long id, Long reviewedBy) {
        PreEnrollment preEnrollment = find(id);
        preEnrollment.approve(reviewedBy);
        return response(preEnrollmentRepository.save(preEnrollment));
    }

    @Override
    @Transactional
    public PreEnrollmentResponse reject(Long id, Long reviewedBy, String reason) {
        PreEnrollment preEnrollment = find(id);
        preEnrollment.reject(reviewedBy, reason);
        return response(preEnrollmentRepository.save(preEnrollment));
    }
    
    //Methode qui vérifie si les frais obligatoires de préinscription ont été payés.
    private boolean hasRequiredFeePayment(PreEnrollment preEnrollment) {
        BigDecimal requiredFee = preEnrollment.getRequiredFee();
        if (requiredFee == null || requiredFee.signum() <= 0) {
            return true;
        }
        BigDecimal paid = feePaymentRepository.sumVerifiedAmountByPreEnrollmentId(preEnrollment.getId());
        return paid != null && paid.compareTo(requiredFee) >= 0;
    }

    //Méthode recherche une préinscription dans le repository.
    private PreEnrollment find(Long id) {
        return preEnrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PreEnrollment", "id", id));
    }

    public PreEnrollmentResponse response(PreEnrollment preEnrollment) {
        return PreEnrollmentResponse.builder()
                .id(preEnrollment.getId())
                .number(preEnrollment.getNumber().getValue())
                .status(preEnrollment.getStatus())
                .academicYearId(preEnrollment.getAcademicYear().getId())
                .requestedLevel(preEnrollment.getRequestedLevel())
                .submittedAt(preEnrollment.getSubmittedAt())
                .build();
    }
}
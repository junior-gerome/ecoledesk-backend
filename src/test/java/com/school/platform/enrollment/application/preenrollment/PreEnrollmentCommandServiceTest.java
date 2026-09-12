package com.school.platform.enrollment.application.preenrollment;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.billing.infrastructure.persistence.PreEnrollmentFeePaymentRepository;
import com.school.platform.enrollment.application.dto.preenrollment.CreatePreEnrollmentRequest;
import com.school.platform.enrollment.application.mapper.PreEnrollmentMapper;
import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.model.RelationshipType;
import com.school.platform.enrollment.domain.policy.RequiredPreEnrollmentDocumentPolicy;
import com.school.platform.enrollment.domain.preenrollment.ApplicantIdentity;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentGuardian;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentNumber;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.shared.application.BusinessAuditService;
import com.school.platform.shared.domain.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PreEnrollmentCommandServiceTest {

    private PreEnrollmentRepository preRepository;
    private AcademicYearRepository yearRepository;
    private PreEnrollmentFeePaymentRepository feePaymentRepository;
    private RequiredPreEnrollmentDocumentPolicy documentPolicy;
    private PreEnrollmentMapper mapper;
    private BusinessAuditService auditService;
    private PreEnrollmentCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        preRepository = mock(PreEnrollmentRepository.class);
        yearRepository = mock(AcademicYearRepository.class);
        feePaymentRepository = mock(PreEnrollmentFeePaymentRepository.class);
        documentPolicy = new RequiredPreEnrollmentDocumentPolicy();
        documentPolicy.setRequiredDocumentTypes(List.of());
        mapper = mock(PreEnrollmentMapper.class);
        auditService = mock(BusinessAuditService.class);
        service = new PreEnrollmentCommandServiceImpl(
                preRepository, yearRepository, feePaymentRepository, documentPolicy, mapper, auditService);

        when(preRepository.save(any(PreEnrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AcademicYear year(boolean open) {
        AcademicYear year = new AcademicYear();
        year.setStatutCode(open);
        return year;
    }

    private PreEnrollment draftWithGuardian(AcademicYear year) {
        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");
        applicant.setBirthDate(LocalDate.of(2016, 3, 10));
        applicant.setGender(Gender.MASCULIN);

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-020"),
                applicant,
                year,
                "CP",
                BigDecimal.ZERO
        );

        PreEnrollmentGuardian guardian = new PreEnrollmentGuardian();
        guardian.setPreEnrollment(pre);
        guardian.setFirstName("Pierre");
        guardian.setLastName("Martin");
        guardian.setPhoneNumber("699556677");
        guardian.setPrimaryContact(true);
        guardian.setRelationshipType(RelationshipType.FATHER);
        pre.getGuardians().add(guardian);
        return pre;
    }

    private CreatePreEnrollmentRequest createRequest() {
        CreatePreEnrollmentRequest request = new CreatePreEnrollmentRequest();
        request.setFirstName("Alice");
        request.setLastName("Martin");
        request.setBirthDate(LocalDate.of(2016, 3, 10));
        request.setGender(Gender.MASCULIN);
        request.setAcademicYearId(1L);
        request.setRequestedLevel("CP");
        return request;
    }

    @Test
    @DisplayName("Refuse la creation d'un dossier si l'annee scolaire est fermee")
    void testCreateDraftRequiresOpenYear() {
        when(yearRepository.findById(1L)).thenReturn(Optional.of(year(false)));

        assertThatThrownBy(() -> service.createDraft(createRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("n'est pas ouverte");
        verify(preRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refuse la soumission d'un dossier si l'annee scolaire est fermee")
    void testSubmitRequiresOpenYear() {
        PreEnrollment pre = draftWithGuardian(year(false));
        when(preRepository.findById(5L)).thenReturn(Optional.of(pre));

        assertThatThrownBy(() -> service.submit(5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("n'est pas ouverte");
        verify(preRepository, never()).save(any());
    }

    @Test
    @DisplayName("Permet la creation d'un dossier sur une annee ouverte et audite l'action")
    void testCreateDraftOnOpenYearSucceeds() {
        AcademicYear open = year(true);
        open.setId(1L);
        when(yearRepository.findById(1L)).thenReturn(Optional.of(open));
        when(preRepository.save(any(PreEnrollment.class)))
                .thenAnswer(invocation -> {
                    PreEnrollment pre = invocation.getArgument(0);
                    pre.setId(10L);
                    return pre;
                });

        var created = service.createDraft(createRequest());

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getRequestedLevel()).isEqualTo("CP");
        verify(auditService).record("PRE_ENROLLMENT_CREATED", "pre_enrollments", 10L);
    }

    @Test
    @DisplayName("Soumission sur annee ouverte passe au statut SUBMITTED et audite l'action")
    void testSubmitOnOpenYearSucceeds() {
        PreEnrollment pre = draftWithGuardian(year(true));
        pre.setId(5L);
        when(preRepository.findById(5L)).thenReturn(Optional.of(pre));

        var submitted = service.submit(5L);

        assertThat(submitted.getStatus()).isEqualTo(PreEnrollmentStatus.SUBMITTED);
        verify(auditService).record("PRE_ENROLLMENT_SUBMITTED", "pre_enrollments", 5L);
    }
}
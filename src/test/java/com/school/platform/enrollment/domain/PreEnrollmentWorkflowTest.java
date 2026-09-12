package com.school.platform.enrollment.domain;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.model.RelationshipType;
import com.school.platform.enrollment.domain.policy.RequiredPreEnrollmentDocumentPolicy;
import com.school.platform.enrollment.domain.preenrollment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreEnrollmentWorkflowTest {

    private RequiredPreEnrollmentDocumentPolicy documentPolicy;
    private AcademicYear academicYear;
    private ApplicantIdentity applicant;

    @BeforeEach
    void setUp() {
        documentPolicy = new RequiredPreEnrollmentDocumentPolicy();
        documentPolicy.setRequiredDocumentTypes(List.of("BIRTH_CERTIFICATE", "REPORT_CARD"));

        academicYear = new AcademicYear();
        academicYear.setLibelleAcademicYear("2026-2027");
        academicYear.setStatutCode(true);

        applicant = new ApplicantIdentity();
        applicant.setFirstName("Jean");
        applicant.setLastName("Dupont");
        applicant.setBirthDate(LocalDate.of(2015, 5, 12));
        applicant.setGender(Gender.MASCULIN);
        applicant.setBirthPlace("Paris");
    }

    private PreEnrollment createDraftWithGuardian() {
        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-001"),
                applicant,
                academicYear,
                "CP",
                BigDecimal.valueOf(50.0)
        );

        PreEnrollmentGuardian guardian = new PreEnrollmentGuardian();
        guardian.setPreEnrollment(pre);
        guardian.setFirstName("Pierre");
        guardian.setLastName("Dupont");
        guardian.setPhoneNumber("+33612345678");
        guardian.setPrimaryContact(true);
        guardian.setRelationshipType(RelationshipType.FATHER);
        pre.getGuardians().add(guardian);

        return pre;
    }

    @Test
    @DisplayName("Soumission échoue si aucun document requis n'est déposé")
    void testSubmitFailsIfMandatoryDocumentsNotSubmitted() {
        PreEnrollment pre = createDraftWithGuardian();
        assertFalse(documentPolicy.mandatoryDocumentsAreSubmitted(pre));
        assertThrows(IllegalStateException.class, () -> pre.submit(true, documentPolicy.mandatoryDocumentsAreSubmitted(pre)));
    }

    @Test
    @DisplayName("Soumission réussit si les documents requis sont déposés, même non encore approuvés")
    void testSubmitSucceedsWhenMandatoryDocumentsSubmitted() {
        PreEnrollment pre = createDraftWithGuardian();

        PreEnrollmentDocument doc1 = PreEnrollmentDocument.submitted(pre, "BIRTH_CERTIFICATE", "/docs/birth.pdf");
        PreEnrollmentDocument doc2 = PreEnrollmentDocument.submitted(pre, "REPORT_CARD", "/docs/report.pdf");
        pre.addDocument(doc1);
        pre.addDocument(doc2);

        assertTrue(documentPolicy.mandatoryDocumentsAreSubmitted(pre));
        assertFalse(documentPolicy.mandatoryDocumentsAreApproved(pre));

        pre.submit(true, documentPolicy.mandatoryDocumentsAreSubmitted(pre));
        assertEquals(PreEnrollmentStatus.SUBMITTED, pre.getStatus());
        assertNotNull(pre.getSubmittedAt());
    }

    @Test
    @DisplayName("Workflow complet : DRAFT -> SUBMITTED -> UNDER_REVIEW -> revue docs -> APPROVED")
    void testFullApprovalWorkflow() {
        PreEnrollment pre = createDraftWithGuardian();

        PreEnrollmentDocument doc1 = PreEnrollmentDocument.submitted(pre, "BIRTH_CERTIFICATE", "/docs/birth.pdf");
        PreEnrollmentDocument doc2 = PreEnrollmentDocument.submitted(pre, "REPORT_CARD", "/docs/report.pdf");
        pre.addDocument(doc1);
        pre.addDocument(doc2);

        // 1. Soumission
        pre.submit(true, documentPolicy.mandatoryDocumentsAreSubmitted(pre));
        assertEquals(PreEnrollmentStatus.SUBMITTED, pre.getStatus());

        // 2. Début de revue
        pre.startReview(100L);
        assertEquals(PreEnrollmentStatus.UNDER_REVIEW, pre.getStatus());

        // 3. Approbation impossible tant que les pièces ne sont pas approuvées
        assertFalse(documentPolicy.mandatoryDocumentsAreApproved(pre));
        assertThrows(IllegalStateException.class, () -> pre.approve(100L, true, documentPolicy.mandatoryDocumentsAreApproved(pre)));

        // 4. Revue et approbation des pièces
        doc1.review(DocumentReviewStatus.APPROVED, 100L, null);
        doc2.review(DocumentReviewStatus.APPROVED, 100L, null);
        assertTrue(documentPolicy.mandatoryDocumentsAreApproved(pre));

        // 5. Approbation du dossier
        pre.approve(100L, true, documentPolicy.mandatoryDocumentsAreApproved(pre));
        assertEquals(PreEnrollmentStatus.APPROVED, pre.getStatus());
        assertEquals(100L, pre.getReviewedBy());
        assertNotNull(pre.getReviewedAt());
    }

    @Test
    @DisplayName("Un document refusé bloque l'approbation du dossier")
    void testRejectedDocumentBlocksApproval() {
        PreEnrollment pre = createDraftWithGuardian();
        PreEnrollmentDocument doc1 = PreEnrollmentDocument.submitted(pre, "BIRTH_CERTIFICATE", "/docs/birth.pdf");
        PreEnrollmentDocument doc2 = PreEnrollmentDocument.submitted(pre, "REPORT_CARD", "/docs/report.pdf");
        pre.addDocument(doc1);
        pre.addDocument(doc2);

        pre.submit(true, documentPolicy.mandatoryDocumentsAreSubmitted(pre));
        pre.startReview(100L);

        doc1.review(DocumentReviewStatus.APPROVED, 100L, null);
        doc2.review(DocumentReviewStatus.REJECTED, 100L, "Document illisible");

        assertFalse(documentPolicy.mandatoryDocumentsAreApproved(pre));
        assertThrows(IllegalStateException.class,
                () -> pre.approve(100L, true, documentPolicy.mandatoryDocumentsAreApproved(pre)));
        assertEquals(PreEnrollmentStatus.UNDER_REVIEW, pre.getStatus());
    }

    @Test
    @DisplayName("Sans documents obligatoires configurés, la soumission reste possible")
    void testSubmitSucceedsWhenNoMandatoryDocumentsConfigured() {
        RequiredPreEnrollmentDocumentPolicy emptyPolicy = new RequiredPreEnrollmentDocumentPolicy();
        emptyPolicy.setRequiredDocumentTypes(List.of());

        PreEnrollment pre = createDraftWithGuardian();
        pre.submit(true, emptyPolicy.mandatoryDocumentsAreSubmitted(pre));

        assertEquals(PreEnrollmentStatus.SUBMITTED, pre.getStatus());
    }

    @Test
    @DisplayName("Rejet du dossier avec motif obligatoire")
    void testRejectWorkflow() {
        PreEnrollment pre = createDraftWithGuardian();
        PreEnrollmentDocument doc1 = PreEnrollmentDocument.submitted(pre, "BIRTH_CERTIFICATE", "/docs/birth.pdf");
        PreEnrollmentDocument doc2 = PreEnrollmentDocument.submitted(pre, "REPORT_CARD", "/docs/report.pdf");
        pre.addDocument(doc1);
        pre.addDocument(doc2);

        pre.submit(true, documentPolicy.mandatoryDocumentsAreSubmitted(pre));
        pre.startReview(100L);

        assertThrows(IllegalArgumentException.class, () -> pre.reject(100L, "  "));
        pre.reject(100L, "Dossier incomplet après relance");
        assertEquals(PreEnrollmentStatus.REJECTED, pre.getStatus());
        assertEquals("Dossier incomplet après relance", pre.getRejectionReason());
    }
}

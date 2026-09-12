package com.school.platform.enrollment.application.policy;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentNumber;
import com.school.platform.enrollment.domain.policy.AdmissionPolicy;
import com.school.platform.enrollment.domain.preenrollment.ApplicantIdentity;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentNumber;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.shared.domain.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdmissionPolicyTest {

    private EnrollmentRepository enrollmentRepository;
    private AdmissionPolicy admissionPolicy;

    @BeforeEach
    void setUp() {
        enrollmentRepository = mock(EnrollmentRepository.class);
        admissionPolicy = new AdmissionPolicy(enrollmentRepository);
        when(enrollmentRepository.countByClassroomIdAndAcademicYearIdAndStatus(anyLong(), anyLong(), any()))
                .thenReturn(0L);
    }

    private Enrollment openEnrollment(String requestedLevel) {
        AcademicYear year = new AcademicYear();
        year.setStatutCode(true);
        year.setId(1L);

        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-003"),
                applicant,
                year,
                requestedLevel,
                BigDecimal.ZERO
        );

        return Enrollment.pending(
                EnrollmentNumber.of("ENR-2026-003"),
                pre,
                year,
                classRoom(requestedLevel, 40)
        );
    }

    private ClasseRoom classRoom(String level, Integer capacity) {
        ClasseRoom classroom = new ClasseRoom();
        classroom.setId(1L);
        classroom.setNameClasse(level + " A");
        classroom.setLevel(level);
        classroom.setCapacity(capacity);
        classroom.setActive(true);
        return classroom;
    }

    @Test
    @DisplayName("Refuse la confirmation si la classe a atteint sa capacite")
    void testClassFullRefusesConfirmation() {
        when(enrollmentRepository.countByClassroomIdAndAcademicYearIdAndStatus(anyLong(), anyLong(), any()))
                .thenReturn(40L);

        Enrollment enrollment = openEnrollment("CP");
        ClasseRoom full = classRoom("CP", 40);

        assertThatThrownBy(() -> admissionPolicy.assertConfirmationAllowed(enrollment, full))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    @DisplayName("Autorise la confirmation pour la derniere place libre")
    void testLastFreePlaceAllowed() {
        when(enrollmentRepository.countByClassroomIdAndAcademicYearIdAndStatus(anyLong(), anyLong(), any()))
                .thenReturn(39L);

        Enrollment enrollment = openEnrollment("CP");

        assertThatCode(() -> admissionPolicy.assertConfirmationAllowed(enrollment, classRoom("CP", 40)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deux confirmations concurrentes pour la derniere place : seule la premiere passe")
    void testConcurrentConfirmationsForLastPlace() {
        AcademicYear year = new AcademicYear();
        year.setStatutCode(true);
        year.setId(1L);

        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Bob");
        applicant.setLastName("Nono");

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-004"),
                applicant,
                year,
                "CE1",
                BigDecimal.ZERO
        );
        Enrollment first = Enrollment.pending(EnrollmentNumber.of("ENR-2026-004"), pre, year, classRoom("CE1", 1));
        Enrollment second = Enrollment.pending(EnrollmentNumber.of("ENR-2026-005"), pre, year, classRoom("CE1", 1));

        when(enrollmentRepository.countByClassroomIdAndAcademicYearIdAndStatus(anyLong(), anyLong(), any()))
                .thenReturn(0L, 1L);

        ClasseRoom singlePlace = classRoom("CE1", 1);

        assertThatCode(() -> admissionPolicy.assertConfirmationAllowed(first, singlePlace))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> admissionPolicy.assertConfirmationAllowed(second, singlePlace))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("capacity");
    }

    @Test
    @DisplayName("Refuse la confirmation si l'annee scolaire n'est pas ouverte")
    void testClosedYearRefusesConfirmation() {
        AcademicYear year = new AcademicYear();
        year.setStatutCode(false);

        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-005"),
                applicant,
                year,
                "CP",
                BigDecimal.ZERO
        );
        Enrollment enrollment = Enrollment.pending(EnrollmentNumber.of("ENR-2026-006"), pre, year, classRoom("CP", 40));

        assertThatThrownBy(() -> admissionPolicy.assertConfirmationAllowed(enrollment, classRoom("CP", 40)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not open");
    }

    @Test
    @DisplayName("Refuse la confirmation si le niveau de la classe differe du niveau demande")
    void testLevelMismatchRefusesConfirmation() {
        Enrollment enrollment = openEnrollment("CE2");

        assertThatThrownBy(() -> admissionPolicy.assertConfirmationAllowed(enrollment, classRoom("CE1", 40)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("level");
    }

    @Test
    @DisplayName("Refuse la confirmation sur une classe desactivee")
    void testInactiveClassroomRefusesConfirmation() {
        Enrollment enrollment = openEnrollment("CP");
        ClasseRoom inactive = classRoom("CP", 40);
        inactive.setActive(false);

        assertThatThrownBy(() -> admissionPolicy.assertConfirmationAllowed(enrollment, inactive))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }
}
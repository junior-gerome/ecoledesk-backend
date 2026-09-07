package com.school.platform.enrollment.domain;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentNumber;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.preenrollment.ApplicantIdentity;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentNumber;
import com.school.platform.identityaccess.domain.model.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentConfirmationTest {

    @Test
    @DisplayName("L'inscription en attente est créée avec student = null")
    void testPendingEnrollmentHasNullStudent() {
        AcademicYear year = new AcademicYear();
        year.setLibelleAcademicYear("2026-2027");

        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");
        applicant.setBirthDate(LocalDate.of(2016, 3, 10));
        applicant.setGender(Gender.FEMININ);

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-002"),
                applicant,
                year,
                "CE1",
                BigDecimal.ZERO
        );

        ClasseRoom classroom = new ClasseRoom();
        classroom.setNameClasse("CE1 A");

        Enrollment enrollment = Enrollment.pending(
                EnrollmentNumber.of("ENR-2026-001"),
                pre,
                year,
                classroom
        );

        assertEquals(EnrollmentStatus.PENDING_CONFIRMATION, enrollment.getStatus());
        assertNull(enrollment.getStudent(), "L'élève ne doit pas encore exister dans l'inscription en attente");
    }

    @Test
    @DisplayName("La confirmation affecte l'élève et passe le statut à CONFIRMED")
    void testConfirmationAttachesStudent() {
        AcademicYear year = new AcademicYear();
        year.setLibelleAcademicYear("2026-2027");

        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");
        applicant.setBirthDate(LocalDate.of(2016, 3, 10));
        applicant.setGender(Gender.FEMININ);

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-002"),
                applicant,
                year,
                "CE1",
                BigDecimal.ZERO
        );

        ClasseRoom classroom = new ClasseRoom();
        classroom.setNameClasse("CE1 A");

        Enrollment enrollment = Enrollment.pending(
                EnrollmentNumber.of("ENR-2026-001"),
                pre,
                year,
                classroom
        );

        Person person = new Person();
        person.setFirstName("Alice");
        person.setLastName("Martin");

        Student student = new Student();
        student.setPerson(person);
        student.setStudentNumber("STU-2026-001");
        student.setAdmissionDate(LocalDate.now());

        enrollment.confirm(student);

        assertEquals(EnrollmentStatus.CONFIRMED, enrollment.getStatus());
        assertNotNull(enrollment.getStudent());
        assertEquals("STU-2026-001", enrollment.getStudent().getStudentNumber());
        assertNotNull(enrollment.getConfirmationDate());
    }
}

package com.school.platform.enrollment.application.enrollment;

import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.enrollment.application.dto.enrollment.CreateEnrollmentFromPreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentResponse;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentNumber;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.domain.model.RelationshipType;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.policy.AdmissionPolicy;
import com.school.platform.enrollment.domain.preenrollment.ApplicantIdentity;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentGuardian;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentNumber;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.GuardianRepository;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;
import com.school.platform.shared.application.BusinessAuditService;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnrollmentCommandServiceTest {

    private PreEnrollmentRepository preRepository;
    private EnrollmentRepository enrollmentRepository;
    private ClasseRoomRepository classRepository;
    private StudentRepository studentRepository;
    private PersonRepository personRepository;
    private GuardianRepository guardianRepository;
    private AdmissionPolicy admissionPolicy;
    private BusinessAuditService auditService;
    private EnrollmentCommandServiceImp service;

    @BeforeEach
    void setUp() {
        preRepository = mock(PreEnrollmentRepository.class);
        enrollmentRepository = mock(EnrollmentRepository.class);
        classRepository = mock(ClasseRoomRepository.class);
        studentRepository = mock(StudentRepository.class);
        personRepository = mock(PersonRepository.class);
        guardianRepository = mock(GuardianRepository.class);
        admissionPolicy = mock(AdmissionPolicy.class);
        auditService = mock(BusinessAuditService.class);
        service = new EnrollmentCommandServiceImp(
                preRepository, enrollmentRepository, classRepository, studentRepository,
                personRepository, guardianRepository, admissionPolicy, auditService);

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(guardianRepository.save(any(Guardian.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    /** Construit un dossier approuve (draft -> soumis -> en revue -> approuve) avec un responsable joignable. */
    private PreEnrollment approvedPreEnrollment(Long id, String guardianPhone) {
        AcademicYear year = openYear();
        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");
        applicant.setBirthDate(LocalDate.of(2016, 3, 10));
        applicant.setGender(Gender.MASCULIN);

        PreEnrollment pre = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-010"),
                applicant,
                year,
                "CP",
                BigDecimal.ZERO
        );
        pre.setId(id);

        PreEnrollmentGuardian guardian = guardian("Pierre", "Martin", guardianPhone);
        guardian.setPreEnrollment(pre);
        pre.getGuardians().add(guardian);

        pre.submit(true, true);
        pre.startReview(1L);
        pre.approve(1L, true, true);
        return pre;
    }

    private PreEnrollmentGuardian guardian(String firstName, String lastName, String phone) {
        PreEnrollmentGuardian guardian = new PreEnrollmentGuardian();
        guardian.setRelationshipType(RelationshipType.FATHER);
        guardian.setFirstName(firstName);
        guardian.setLastName(lastName);
        guardian.setPhoneNumber(phone);
        guardian.setPrimaryContact(true);
        return guardian;
    }

    private AcademicYear openYear() {
        AcademicYear year = new AcademicYear();
        year.setStatutCode(true);
        return year;
    }

    private ClasseRoom classRoom(String level, Integer capacity) {
        ClasseRoom classroom = new ClasseRoom();
        classroom.setId(7L);
        classroom.setNameClasse(level + " A");
        classroom.setLevel(level);
        classroom.setCapacity(capacity);
        classroom.setActive(true);
        return classroom;
    }

    private Enrollment pendingEnrollment(Long id, PreEnrollment pre, ClasseRoom classroom) {
        Enrollment enrollment = Enrollment.pending(
                EnrollmentNumber.of("ENR-2026-" + id),
                pre,
                pre.getAcademicYear(),
                classroom
        );
        enrollment.setId(id);
        return enrollment;
    }

    @Test
    @DisplayName("La creation d'inscription depuis une pre-inscription approuvee ne cree aucun eleve")
    void testCreateEnrollmentKeepsStudentNull() {
        PreEnrollment pre = approvedPreEnrollment(20L, "699223344");
        when(preRepository.findById(20L)).thenReturn(Optional.of(pre));
        when(enrollmentRepository.existsByPreEnrollmentId(20L)).thenReturn(false);
        ClasseRoom classroom = classRoom("CP", 40);
        when(classRepository.findById(7L)).thenReturn(Optional.of(classroom));

        CreateEnrollmentFromPreEnrollmentRequest request = new CreateEnrollmentFromPreEnrollmentRequest();
        request.setClassroomId(7L);
        EnrollmentResponse response = service.createFromApprovedPreEnrollment(20L, request);

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING_CONFIRMATION);
        assertThat(response.getStudentId()).isNull();
        assertThat(response.getPreEnrollmentId()).isEqualTo(20L);
        verify(studentRepository, never()).save(any(Student.class));
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    @DisplayName("Refuse de creer une inscription pour un dossier non approuve")
    void testCreateEnrollmentRequiresApprovedPreEnrollment() {
        AcademicYear year = openYear();
        ApplicantIdentity applicant = new ApplicantIdentity();
        applicant.setFirstName("Alice");
        applicant.setLastName("Martin");
        PreEnrollment draft = PreEnrollment.draft(
                PreEnrollmentNumber.of("PRE-2026-011"),
                applicant,
                year,
                "CP",
                BigDecimal.ZERO
        );
        when(preRepository.findById(21L)).thenReturn(Optional.of(draft));

        CreateEnrollmentFromPreEnrollmentRequest request = new CreateEnrollmentFromPreEnrollmentRequest();
        request.setClassroomId(7L);

        assertThatThrownBy(() -> service.createFromApprovedPreEnrollment(21L, request))
                .isInstanceOf(BadRequestException.class);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("La confirmation cree l'eleve et passe l'inscription a CONFIRMED")
    void testConfirmCreatesStudentExactlyOnce() {
        PreEnrollment pre = approvedPreEnrollment(22L, "699334455");
        ClasseRoom classroom = classRoom("CP", 40);
        Enrollment pending = pendingEnrollment(1L, pre, classroom);

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(classRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(classroom));

        EnrollmentResponse confirmed = service.confirm(1L);

        assertThat(confirmed.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(confirmed.getStudentId()).isNull();
        verify(studentRepository, times(2)).save(any(Student.class));
        verify(personRepository, times(1)).save(any(Person.class));
        verify(auditService).record("ENROLLMENT_CONFIRMED", "enrollments", 1L);
    }

    @Test
    @DisplayName("Une classe complete refuse la confirmation sans creer d'eleve")
    void testConfirmRefusesFullClassWithoutCreatingStudent() {
        PreEnrollment pre = approvedPreEnrollment(23L, "699445566");
        ClasseRoom fullClass = classRoom("CP", 40);
        Enrollment pending = pendingEnrollment(2L, pre, fullClass);

        when(enrollmentRepository.findById(2L)).thenReturn(Optional.of(pending));
        when(classRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(fullClass));
        doThrow(new BadRequestException("Class capacity has been reached"))
                .when(admissionPolicy).assertConfirmationAllowed(pending, fullClass);

        assertThatThrownBy(() -> service.confirm(2L))
                .isInstanceOf(BadRequestException.class);

        verify(studentRepository, never()).save(any(Student.class));
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    @DisplayName("Recherche d'un responsable existant par telephone normalise a la confirmation")
    void testGuardianReusedByNormalizedPhone() {
        PreEnrollment pre = approvedPreEnrollment(24L, "+237 6 55 12 34 56");
        ClasseRoom classroom = classRoom("CP", 40);
        Enrollment pending = pendingEnrollment(3L, pre, classroom);

        Guardian existing = new Guardian();
        existing.setId(99L);

        when(enrollmentRepository.findById(3L)).thenReturn(Optional.of(pending));
        when(classRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(classroom));
        when(guardianRepository.findByPhoneNumber("+237655123456")).thenReturn(Optional.of(existing));

        service.confirm(3L);

        verify(guardianRepository).findByPhoneNumber("+237655123456");
        verify(guardianRepository, never()).save(any(Guardian.class));
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    @DisplayName("Annulation d'une inscription en attente sans creation d'eleve")
    void testCancelPendingEnrollmentDoesNotCreateStudent() {
        PreEnrollment pre = approvedPreEnrollment(25L, "699556677");
        ClasseRoom classroom = classRoom("CP", 40);
        Enrollment pending = pendingEnrollment(4L, pre, classroom);

        when(enrollmentRepository.findById(4L)).thenReturn(Optional.of(pending));

        EnrollmentResponse cancelled = service.cancel(4L, "Retrait de la candidature");

        assertThat(cancelled.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        verify(studentRepository, never()).save(any(Student.class));
        verify(auditService).record("ENROLLMENT_CANCELLED", "enrollments", 4L);
    }

    @Test
    @DisplayName("L'inscription introuvable leve une ResourceNotFoundException")
    void testUnknownEnrollmentThrows() {
        when(enrollmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(404L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.cancel(404L, "raison"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
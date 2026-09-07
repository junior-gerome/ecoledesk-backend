package com.school.platform.enrollment.application.enrollment;

import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.enrollment.application.dto.enrollment.CreateEnrollmentFromPreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentResponse;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentNumber;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.model.StudentGuardian;
import com.school.platform.enrollment.domain.policy.AdmissionPolicy;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentGuardian;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.GuardianRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentCommandServiceImp implements EnrollmentCommandService {

    private final PreEnrollmentRepository preRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClasseRoomRepository classRepository;
    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;
    private final GuardianRepository guardianRepository;
    private final AdmissionPolicy admissionPolicy;

    @Override
    @Transactional
    public EnrollmentResponse createFromApprovedPreEnrollment(
            Long preEnrollmentId, CreateEnrollmentFromPreEnrollmentRequest request) {
        PreEnrollment preEnrollment = preRepository.findById(preEnrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("PreEnrollment", "id", preEnrollmentId));
        if (preEnrollment.getStatus() != PreEnrollmentStatus.APPROVED) {
            throw new BadRequestException("Only an approved pre-enrollment can create an enrollment");
        }
        if (enrollmentRepository.existsByPreEnrollmentId(preEnrollmentId)) {
            throw new BadRequestException("An enrollment already exists for this pre-enrollment");
        }
        ClasseRoom classroom = classRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe", "id", request.getClassroomId()));
        Enrollment enrollment = Enrollment.pending(
                EnrollmentNumber.of(nextNumber()), preEnrollment, preEnrollment.getAcademicYear(), classroom);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse confirm(Long enrollmentId) {
        Enrollment enrollment = find(enrollmentId);
        if (enrollment.getStatus() != EnrollmentStatus.PENDING_CONFIRMATION) {
            throw new BadRequestException("Only pending enrollments can be confirmed");
        }
        ClasseRoom lockedClassroom = classRepository.findByIdForUpdate(enrollment.getClassroom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe", "id", enrollment.getClassroom().getId()));
        admissionPolicy.assertConfirmationAllowed(enrollment, lockedClassroom);
        Student student = enrollment.getStudent();
        if (student == null) {
            student = createStudent(enrollment.getPreEnrollment());
        }
        enrollment.confirm(student);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse cancel(Long enrollmentId, String reason) {
        Enrollment enrollment = find(enrollmentId);
        enrollment.cancel(reason);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentResponse withdraw(Long enrollmentId) {
        Enrollment enrollment = find(enrollmentId);
        enrollment.withdraw();
        return toResponse(enrollmentRepository.save(enrollment));
    }

    private Student createStudent(PreEnrollment preEnrollment) {
        Person person = new Person();
        person.setFirstName(preEnrollment.getApplicant().getFirstName());
        person.setLastName(preEnrollment.getApplicant().getLastName());
        person.setBirthDate(preEnrollment.getApplicant().getBirthDate());
        person.setGender(preEnrollment.getApplicant().getGender());

        Student student = new Student();
        student.setPerson(person);
        student.setStudentNumber("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        student.setAdmissionDate(LocalDate.now());
        student.setActive(true);
        Student savedStudent = studentRepository.save(student);

        for (PreEnrollmentGuardian source : preEnrollment.getGuardians()) {
            Guardian guardian = findOrCreateGuardian(source);
            StudentGuardian link = new StudentGuardian();
            link.setStudent(savedStudent);
            link.setGuardian(guardian);
            link.setRelationshipType(source.getRelationshipType());
            link.setRelationshipDetails(source.getRelationshipDetails());
            link.setPrimaryContact(source.isPrimaryContact());
            link.setFinancialResponsible(source.isFinancialResponsible());
            link.setEmergencyContact(source.isEmergencyContact());
            savedStudent.getStudentGuardians().add(link);
            guardian.getStudentGuardians().add(link);
        }
        return studentRepository.save(savedStudent);
    }

    private Guardian findOrCreateGuardian(PreEnrollmentGuardian source) {
        if (source.getEmail() != null && !source.getEmail().isBlank()) {
            Guardian existing = guardianRepository.findByEmail(source.getEmail()).orElse(null);
            if (existing != null) return existing;
        }
        Person person = source.getEmail() == null || source.getEmail().isBlank()
                ? null
                : personRepository.findByEmail(source.getEmail()).orElse(null);
        if (person == null) {
            person = new Person();
            person.setFirstName(source.getFirstName());
            person.setLastName(source.getLastName());
            if (source.getEmail() != null && !source.getEmail().isBlank()) person.setEmail(source.getEmail());
            if (source.getPhoneNumber() != null && !source.getPhoneNumber().isBlank()) person.setPhone(source.getPhoneNumber());
            if (source.getAddress() != null && !source.getAddress().isBlank()) person.setAddress(source.getAddress());
            person = personRepository.save(person);
        }
        Guardian guardian = new Guardian();
        guardian.setPerson(person);
        return guardianRepository.save(guardian);
    }

    private Enrollment find(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
    }

    private String nextNumber() {
        return "ENR-" + Year.now().getValue() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .number(enrollment.getNumber().getValue())
                .preEnrollmentId(enrollment.getPreEnrollment().getId())
                .studentId(enrollment.getStudent() == null ? null : enrollment.getStudent().getId())
                .classroomId(enrollment.getClassroom() == null ? null : enrollment.getClassroom().getId())
                .status(enrollment.getStatus())
                .build();
    }
}

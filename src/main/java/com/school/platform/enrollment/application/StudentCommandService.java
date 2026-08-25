package com.school.platform.enrollment.application;

import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.mapper.GuardianMapper;
import com.school.platform.enrollment.application.mapper.StudentMapper;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.GuardianRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCommandService {
    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final StudentMapper studentMapper;
    private final GuardianMapper guardianMapper;

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDTO createStudentWithGuardian(StudentDTO dto) {
        requireGuardian(dto);
        Guardian guardian = resolveGuardianForCreate(dto.getGuardian());
        Student student = studentMapper.toEntity(dto);
        student.setGuardian(guardian);
        student.setRegistrationDate(LocalDateTime.now());
        student.setActive(true);
        // Génère un matricule unique si non fourni
        if (student.getStudentNumber() == null || student.getStudentNumber().isBlank()) {
            student.setStudentNumber(generateStudentNumber());
        }
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
        studentMapper.updateEntityFromDto(dto, student);
        if (dto.getGuardian() != null) student.setGuardian(resolveGuardianForUpdate(student, dto.getGuardian()));
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
        student.setActive(false);
        studentRepository.save(student);
    }

    @Transactional
    @CacheEvict(value = "students", key = "#studentId")
    public StudentDTO linkGuardian(Long studentId, Long guardianId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + studentId));
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new ResourceNotFoundException("Responsable legal non trouve avec l'ID : " + guardianId));
        student.setGuardian(guardian);
        return studentMapper.toDto(studentRepository.save(student));
    }

    private Guardian resolveGuardianForCreate(GuardianDTO dto) {
        return findExistingGuardian(dto).orElseGet(() -> guardianRepository.save(guardianMapper.toEntity(dto)));
    }

    private Guardian resolveGuardianForUpdate(Student student, GuardianDTO dto) {
        Guardian guardian = dto.getId() == null ? findExistingGuardian(dto).orElse(student.getGuardian())
                : guardianRepository.findById(dto.getId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Responsable legal non trouve avec l'ID : " + dto.getId()));
        if (guardian == null) return guardianRepository.save(guardianMapper.toEntity(dto));
        validateContactUniqueness(dto, guardian.getId());
        guardianMapper.updateEntityFromDto(dto, guardian);
        return guardianRepository.save(guardian);
    }

    private Optional<Guardian> findExistingGuardian(GuardianDTO dto) {
        if (dto == null) return Optional.empty();
        if (hasText(dto.getEmail())) {
            Optional<Guardian> guardian = guardianRepository.findByEmail(dto.getEmail().trim());
            if (guardian.isPresent()) return guardian;
        }
        return hasText(dto.getPhoneNumber()) ? guardianRepository.findByPhoneNumber(dto.getPhoneNumber().trim()) : Optional.empty();
    }

    private void validateContactUniqueness(GuardianDTO dto, Long id) {
        if (hasText(dto.getEmail())) guardianRepository.findByEmail(dto.getEmail().trim()).filter(g -> !g.getId().equals(id))
                .ifPresent(g -> { throw new IllegalArgumentException("Un autre responsable legal utilise deja cet email."); });
        if (hasText(dto.getPhoneNumber())) guardianRepository.findByPhoneNumber(dto.getPhoneNumber().trim()).filter(g -> !g.getId().equals(id))
                .ifPresent(g -> { throw new IllegalArgumentException("Un autre responsable legal utilise deja ce numero de telephone."); });
    }

    private void requireGuardian(StudentDTO dto) {
        if (dto == null || dto.getGuardian() == null) throw new IllegalArgumentException("Les informations de l'eleve et du responsable legal sont requises.");
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }

    /**
     * Génère un matricule unique au format GSBP-XXXXXX (6 chiffres séquentiels zéro-paddés).
     * Exemple : GSBP-000001, GSBP-000042
     */
    private String generateStudentNumber() {
        long count = studentRepository.count() + 1;
        return String.format("GSBP-%06d", count);
    }
}

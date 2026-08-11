package com.school.platform.enrollment.application.impl;

import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.dto.student.StudentBasicDTO;
import com.school.platform.enrollment.application.dto.student.StudentFullDTO;
import com.school.platform.enrollment.application.dto.student.StudentMediumDTO;
import com.school.platform.enrollment.application.interfaces.StudentService;
import com.school.platform.enrollment.application.mapper.GuardianMapper;
import com.school.platform.enrollment.application.mapper.StudentMapper;
import com.school.platform.enrollment.application.mapper.StudentProjectionMapper;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.GuardianRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.shared.domain.exception.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentMapper studentMapper;
    private final GuardianMapper guardianMapper;
    private final StudentProjectionMapper projectionMapper;

    // ── Legacy ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDTO createStudentWithGuardian(StudentDTO dto) {
        requireGuardian(dto);
        Guardian guardian = resolveGuardianForCreate(dto.getGuardian());
        Student student = studentMapper.toEntity(dto);
        student.setGuardian(guardian);
        student.setRegistrationDate(LocalDateTime.now());
        student.setActive(true);
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Override
    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
        studentMapper.updateEntityFromDto(dto, student);
        if (dto.getGuardian() != null) student.setGuardian(resolveGuardianForUpdate(student, dto.getGuardian()));
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Override
    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
        student.setActive(false);
        studentRepository.save(student);
    }

    @Override
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

    // ── Projections ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<StudentBasicDTO> getAllBasic() {
        return projectionMapper.toBasicDTOList(studentRepository.findByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentMediumDTO> getAllMedium(Pageable pageable) {
        return studentRepository.findByActiveTrue(pageable).map(projectionMapper::toMediumDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentFullDTO getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student", "id", id));
        return projectionMapper.toFullDTO(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentMediumDTO> getByClassroom(Long classroomId, Pageable pageable) {
        return enrollmentRepository
                .findByClassroomIdAndStatus(classroomId, EnrollmentStatus.CONFIRMED, pageable)
                .map(e -> projectionMapper.toMediumDTO(e.getStudent()));
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return studentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        long total = studentRepository.count();
        long active = studentRepository.findByActiveTrue().size();
        long confirmed = enrollmentRepository.countByStatus(EnrollmentStatus.CONFIRMED);
        return Map.of("total", total, "active", active, "confirmedEnrollments", confirmed);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Guardian resolveGuardianForCreate(GuardianDTO dto) {
        return findExistingGuardian(dto).orElseGet(() -> guardianRepository.save(guardianMapper.toEntity(dto)));
    }

    private Guardian resolveGuardianForUpdate(Student student, GuardianDTO dto) {
        Guardian guardian = dto.getId() == null
                ? findExistingGuardian(dto).orElse(student.getGuardian())
                : guardianRepository.findById(dto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Responsable legal non trouve avec l'ID : " + dto.getId()));
        if (guardian == null) return guardianRepository.save(guardianMapper.toEntity(dto));
        validateContactUniqueness(dto, guardian.getId());
        guardianMapper.updateEntityFromDto(dto, guardian);
        return guardianRepository.save(guardian);
    }

    private Optional<Guardian> findExistingGuardian(GuardianDTO dto) {
        if (dto == null) return Optional.empty();
        if (hasText(dto.getEmail())) {
            Optional<Guardian> g = guardianRepository.findByEmail(dto.getEmail().trim());
            if (g.isPresent()) return g;
        }
        return hasText(dto.getPhoneNumber())
                ? guardianRepository.findByPhoneNumber(dto.getPhoneNumber().trim())
                : Optional.empty();
    }

    private void validateContactUniqueness(GuardianDTO dto, Long id) {
        if (hasText(dto.getEmail()))
            guardianRepository.findByEmail(dto.getEmail().trim())
                    .filter(g -> !g.getId().equals(id))
                    .ifPresent(g -> { throw new IllegalArgumentException("Un autre responsable legal utilise deja cet email."); });
        if (hasText(dto.getPhoneNumber()))
            guardianRepository.findByPhoneNumber(dto.getPhoneNumber().trim())
                    .filter(g -> !g.getId().equals(id))
                    .ifPresent(g -> { throw new IllegalArgumentException("Un autre responsable legal utilise deja ce numero de telephone."); });
    }

    private void requireGuardian(StudentDTO dto) {
        if (dto == null || dto.getGuardian() == null)
            throw new IllegalArgumentException("Les informations de l'eleve et du responsable legal sont requises.");
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}

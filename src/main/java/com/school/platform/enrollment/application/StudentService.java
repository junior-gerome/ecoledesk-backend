package com.school.platform.enrollment.application;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.enrollment.application.dto.ParentDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.reporting.application.dto.StudentReportDTO;
import com.school.platform.reporting.application.dto.SubjectGradeDTO;
import com.school.platform.enrollment.application.mapper.ParentMapper;
import com.school.platform.enrollment.application.mapper.StudentMapper;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.enrollment.domain.model.InscriptionStudent;
import com.school.platform.enrollment.domain.model.Parent;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.academic.domain.model.Teacher;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.enrollment.infrastructure.persistence.ParentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final GradeRepository gradeRepository;
    private final AbsenceRepository absenceRepository;
    private final InscriptionStudentRepository inscriptionStudentRepository;
    private final StudentMapper studentMapper;
    private final ParentMapper parentMapper;

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDTO createStudentWithParent(StudentDTO studentDTO) {
        if (studentDTO == null || studentDTO.getParent() == null) {
            throw new IllegalArgumentException("Les informations de l'eleve et du parent sont requises.");
        }

        Parent parentEntity = resolveParentForCreate(studentDTO.getParent());

        Student studentEntity = studentMapper.toEntity(studentDTO);
        studentEntity.setParent(parentEntity);
        studentEntity.setRegistrationDate(LocalDateTime.now());
        studentEntity.setActive(true);

        Student savedStudent = studentRepository.save(studentEntity);
        log.info("Eleve enregistre : {} {} (ID: {})", savedStudent.getFirstNameStudent(),
                savedStudent.getLastNameStudent(), savedStudent.getId());

        return studentMapper.toDto(savedStudent);
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByParentId(Long parentId, Pageable pageable) {
        return studentRepository.findByParentId(parentId, pageable)
                .map(studentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findByActiveTrue().stream()
                .map(studentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students", key = "#id")
    public StudentDTO getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(studentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
    }

    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les informations de l'eleve sont requises.");
        }

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));

        studentMapper.updateEntityFromDto(dto, existingStudent);

        if (dto.getParent() != null) {
            Parent resolvedParent = resolveParentForUpdate(existingStudent, dto.getParent());
            existingStudent.setParent(resolvedParent);
        }

        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("Etudiant mis a jour : {}", updatedStudent.getId());

        return studentMapper.toDto(updatedStudent);
    }

    private Parent resolveParentForCreate(ParentDTO parentDto) {
        Optional<Parent> existingParent = findExistingParentByEmailOrPhone(parentDto);
        if (existingParent.isPresent()) {
            Parent parentEntity = existingParent.get();
            log.info("Parent existant trouve : {}", parentEntity.getEmail());
            return parentEntity;
        }

        Parent parentEntity = parentMapper.toEntity(parentDto);
        Parent savedParent = parentRepository.save(parentEntity);
        parentRepository.flush();
        log.info("Nouveau parent cree : {}", savedParent.getEmail());
        return savedParent;
    }

    private Parent resolveParentForUpdate(Student existingStudent, ParentDTO parentDto) {
        Parent parentEntity = null;

        if (parentDto.getId() != null) {
            parentEntity = parentRepository.findById(parentDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent non trouve avec l'ID : " + parentDto.getId()));
        }

        if (parentEntity == null) {
            parentEntity = findExistingParentByEmailOrPhone(parentDto)
                    .orElse(existingStudent.getParent());
        }

        if (parentEntity == null) {
            Parent createdParent = parentRepository.save(parentMapper.toEntity(parentDto));
            log.info("Nouveau parent cree pendant la mise a jour : {}", createdParent.getId());
            return createdParent;
        }

        validateParentContactUniqueness(parentDto, parentEntity.getId());
        parentMapper.updateEntityFromDto(parentDto, parentEntity);
        return parentRepository.save(parentEntity);
    }

    private Optional<Parent> findExistingParentByEmailOrPhone(ParentDTO parentDto) {
        if (parentDto == null) {
            return Optional.empty();
        }

        if (hasText(parentDto.getEmail())) {
            Optional<Parent> existingByEmail = parentRepository.findByEmail(parentDto.getEmail().trim());
            if (existingByEmail.isPresent()) {
                return existingByEmail;
            }
        }

        if (hasText(parentDto.getPhoneNumber())) {
            return parentRepository.findByPhoneNumber(parentDto.getPhoneNumber().trim());
        }

        return Optional.empty();
    }

    private void validateParentContactUniqueness(ParentDTO parentDto, Long currentParentId) {
        if (parentDto == null) {
            return;
        }

        if (hasText(parentDto.getEmail())) {
            parentRepository.findByEmail(parentDto.getEmail().trim())
                    .filter(existing -> !existing.getId().equals(currentParentId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Un autre parent utilise deja cet email.");
                    });
        }

        if (hasText(parentDto.getPhoneNumber())) {
            parentRepository.findByPhoneNumber(parentDto.getPhoneNumber().trim())
                    .filter(existing -> !existing.getId().equals(currentParentId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Un autre parent utilise deja ce numero de telephone.");
                    });
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
        student.setActive(false);
        studentRepository.save(student);
        log.info("Etudiant archive avec ID: {}", id);
    }

    @Transactional(readOnly = true)
    public StudentReportDTO generateStudentReport(String studentId, String period) {
        Long parsedStudentId;
        try {
            parsedStudentId = Long.parseLong(studentId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Identifiant eleve invalide: " + studentId);
        }

        Student student = studentRepository.findById(parsedStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + parsedStudentId));

        List<Grade> grades = gradeRepository.findByStudentId(parsedStudentId).stream()
                .filter(grade -> periodMatches(grade, period))
                .collect(Collectors.toList());

        StudentReportDTO report = new StudentReportDTO();
        report.setId(student.getId());
        report.setLastNameStudent(student.getLastNameStudent());
        report.setFirstNameStudent(student.getFirstNameStudent());
        report.setPeriod(period);
        report.setReportDate(LocalDate.now());
        report.setAbsenceDays(absenceRepository.countByStudentId(parsedStudentId));

        report.setGrades(grades.stream()
                .map(this::toSubjectGradeDTO)
                .collect(Collectors.toList()));

        double average = grades.stream()
                .map(Grade::getGrade)
                .mapToDouble(value -> value != null ? value.doubleValue() : 0.0)
                .average()
                .orElse(0.0);
        report.setAverageGrade(average);
        report.setGeneralAverage(average);

        Grade latestGrade = grades.stream()
                .max(Comparator.comparing(
                        grade -> grade.getAssessmentDate() != null ? grade.getAssessmentDate() : LocalDateTime.MIN))
                .orElse(null);

        if (latestGrade != null && latestGrade.getClasse() != null) {
            ClasseRoom classe = latestGrade.getClasse();
            report.setNameClasse(classe.getNameClasse());

            if (classe.getSection() != null || classe.getAnneeScolaire() != null) {
                String section = classe.getSection() != null ? classe.getSection().getLibelle() : "";
                String annee = classe.getAnneeScolaire() != null ? classe.getAnneeScolaire().getLibelleAnneeScolaire() : "";
                report.setLibelleanneeSection((section + " " + annee).trim());
            }

            Teacher teacher = classe.getTeacher();
            if (teacher != null) {
                report.setLastnameTeacher(teacher.getLastnameTeacher());
                report.setFirstnameteacher(teacher.getFirstnameTeacher());
            }

            int rank = gradeRepository.calculateStudentRank(parsedStudentId, classe.getId()).orElse(0);
            int totalStudents = (int) gradeRepository.findByClasseId(classe.getId()).stream()
                    .map(grade -> grade.getStudent() != null ? grade.getStudent().getId() : null)
                    .filter(id -> id != null)
                    .distinct()
                    .count();

            report.setRank(rank);
            report.setTotalStudents(totalStudents);
        } else {
            report.setRank(0);
            report.setTotalStudents(0);
            report.setNameClasse("N/A");
            report.setLibelleanneeSection("N/A");
        }

        report.setTeacherComments(buildTeacherComment(average));
        report.setAttendance(report.getAbsenceDays() > 5 ? "Irregulier" : "Regulier");

        return report;
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getCurrentStudent() {
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public long getTotalStudents() {
        return studentRepository.count();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStatistics() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<InscriptionStudent> inscriptions = inscriptionStudentRepository.findAll();
        Map<Long, InscriptionStudent> latestEnrollmentByStudent = inscriptions.stream()
                .filter(inscription -> inscription.getStudent() != null && inscription.getStudent().getId() != null)
                .collect(Collectors.toMap(
                        inscription -> inscription.getStudent().getId(),
                        inscription -> inscription,
                        (left, right) -> left.getId() != null && right.getId() != null && left.getId() > right.getId()
                                ? left
                                : right));

        long francophone = 0;
        long anglophone = 0;
        long newFrancophone = 0;
        long newAnglophone = 0;

        for (InscriptionStudent inscription : latestEnrollmentByStudent.values()) {
            String section = sectionLabel(inscription);
            boolean isNew = inscription.getDateInscription() != null
                    && !inscription.getDateInscription().isBefore(monthStart);
            if (section.contains("ANGLO")) {
                anglophone++;
                if (isNew) {
                    newAnglophone++;
                }
            } else {
                francophone++;
                if (isNew) {
                    newFrancophone++;
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("francophoneStudents", francophone);
        stats.put("anglophoneStudents", anglophone);
        stats.put("newFrancophoneStudents", newFrancophone);
        stats.put("newAnglophoneStudents", newAnglophone);
        stats.put("monthlyTrend", monthlyTrend(inscriptions));
        return stats;
    }

    private List<Long> monthlyTrend(List<InscriptionStudent> inscriptions) {
        YearMonth currentMonth = YearMonth.now();
        List<Long> trend = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            long count = inscriptions.stream()
                    .filter(inscription -> inscription.getDateInscription() != null)
                    .filter(inscription -> YearMonth.from(inscription.getDateInscription()).equals(month))
                    .map(InscriptionStudent::getStudent)
                    .filter(student -> student != null && student.getId() != null)
                    .map(Student::getId)
                    .distinct()
                    .count();
            trend.add(count);
        }

        return trend;
    }

    private String sectionLabel(InscriptionStudent inscription) {
        if (inscription.getClasseRoom() == null || inscription.getClasseRoom().getSection() == null) {
            return "";
        }
        return String.valueOf(inscription.getClasseRoom().getSection().getLibelle()).toUpperCase();
    }

    private SubjectGradeDTO toSubjectGradeDTO(Grade grade) {
        return SubjectGradeDTO.builder()
                .id(grade.getId())
                .subject(grade.getSubject())
                .sequence(grade.getSequence())
                .classe(grade.getClasse())
                .grade(grade)
                .build();
    }

    private boolean periodMatches(Grade grade, String period) {
        if (!hasText(period)) {
            return true;
        }
        if (hasText(grade.getPeriod()) && grade.getPeriod().equalsIgnoreCase(period.trim())) {
            return true;
        }
        return grade.getSequence() != null
                && hasText(grade.getSequence().getLibelleSequence())
                && grade.getSequence().getLibelleSequence().equalsIgnoreCase(period.trim());
    }

    private String buildTeacherComment(double average) {
        if (average >= 14.0) {
            return "Tres bon travail.";
        }
        if (average >= 10.0) {
            return "Resultats satisfaisants, poursuivez les efforts.";
        }
        return "Resultats insuffisants, plus d'efforts sont necessaires.";
    }
}

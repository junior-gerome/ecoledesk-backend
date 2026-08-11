package com.school.platform.enrollment.application;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.reporting.application.dto.StudentReportDTO;
import com.school.platform.reporting.application.dto.SubjectGradeDTO;
import com.school.platform.enrollment.application.mapper.StudentMapper;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AbsenceRepository absenceRepository;
    private final StudentStatisticsService studentStatisticsService;
    private final StudentMapper studentMapper;
    private final StudentCommandService studentCommandService;

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDTO createStudentWithGuardian(StudentDTO studentDTO) {
        return studentCommandService.createStudentWithGuardian(studentDTO);
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByGuardianId(Long guardianId, Pageable pageable) {
        return studentRepository.findByGuardianId(guardianId, pageable)
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
        return studentCommandService.updateStudent(id, dto);
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

            if (classe.getSection() != null || classe.getAcademicYear() != null) {
                String section = classe.getSection() != null ? classe.getSection().getLibelle() : "";
                String year = classe.getAcademicYear() != null ? classe.getAcademicYear().getLibelleAcademicYear() : "";
                report.setLibelleyearSection((section + " " + year).trim());
            }

            StaffMember teacher = classe.getTeacher();
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
            report.setLibelleyearSection("N/A");
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
        return studentStatisticsService.getStudentStatistics();
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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

package com.school.platform.reporting.application;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.reporting.application.dto.StudentReportDTO;
import com.school.platform.reporting.application.dto.SubjectGradeDTO;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.academic.domain.model.Teacher;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentReportService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AbsenceRepository absenceRepository;

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
        report.setGrades(grades.stream().map(this::toSubjectGradeDTO).collect(Collectors.toList()));

        double average = grades.stream()
                .map(Grade::getGrade)
                .mapToDouble(value -> value != null ? value.doubleValue() : 0.0)
                .average()
                .orElse(0.0);
        report.setAverageGrade(average);
        report.setGeneralAverage(average);

        Grade latestGrade = grades.stream()
                .max(Comparator.comparing(grade -> grade.getAssessmentDate() != null ? grade.getAssessmentDate() : LocalDateTime.MIN))
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
        if (period == null || period.isBlank()) {
            return true;
        }
        if (grade.getPeriod() != null && grade.getPeriod().equalsIgnoreCase(period.trim())) {
            return true;
        }
        return grade.getSequence() != null
                && grade.getSequence().getLibelleSequence() != null
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

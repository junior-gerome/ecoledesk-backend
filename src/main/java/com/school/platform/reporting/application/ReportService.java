package com.school.platform.reporting.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.school.platform.academic.application.GradeService;
import com.school.platform.enrollment.application.StudentService;

import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.billing.application.PaymentService;
import com.school.platform.reporting.application.dto.ClassPerformanceReport;
import com.school.platform.academic.application.dto.GradeDTO;
import com.school.platform.billing.application.dto.PaymentSummaryReport;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.reporting.application.dto.StudentProgressReport;
import com.school.platform.reporting.application.dto.StudentReportDTO;
import com.school.platform.reporting.application.dto.SubjectGradeDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudentService studentService;
    private final GradeService gradeService;
    private final PaymentService paymentService;
    private final PdfGenerationService pdfGenerationService;

    @Transactional(readOnly = true)
    public byte[] generateClassReport(Long classId, String period) {
        try {
            ClassPerformanceReport report = gradeService.generateClassPerformanceReport(classId, period);
            return pdfGenerationService.generateClassPerformanceReport(report);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la generation du rapport de classe", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generatePaymentReport(LocalDate startDate, LocalDate endDate) {
        try {
            PaymentSummaryReport report = paymentService.generatePaymentSummaryReport(startDate, endDate);
            return pdfGenerationService.generatePaymentSummaryReport(report);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la generation du rapport de paiement", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentProgressReport(Long studentId) {
        try {
            StudentReportDTO report = studentService.generateStudentReport(String.valueOf(studentId), null);
            StudentProgressReport progress = toStudentProgressReport(report);
            return pdfGenerationService.generateStudentReport(progress);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la generation du rapport de progression de l'etudiant", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentReport(String studentId, String period) {
        try {
            StudentReportDTO report = studentService.generateStudentReport(studentId, period);
            return pdfGenerationService.generateReportCard(report);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la generation du bulletin de l'etudiant", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentReport(Long studentId, String period) {
        return generateStudentReport(String.valueOf(studentId), period);
    }

    private StudentProgressReport toStudentProgressReport(StudentReportDTO report) {
        StudentProgressReport progress = new StudentProgressReport();

        StudentDTO student = report.getId() != null ? studentService.getStudentById(report.getId()) : null;
        progress.setStudent(student);
        progress.setClassLevel(report.getNameClasse());
        progress.setLibelleanneeSection(report.getLibelleanneeSection());
        progress.setAverageGrade(report.getAverageGrade());
        progress.setRank(report.getRank());
        progress.setTotalStudents(report.getTotalStudents());
        progress.setAbsenceDays(report.getAbsenceDays());
        progress.setLastnameTeacher(report.getLastnameTeacher());
        progress.setPeriod(report.getPeriod());

        List<GradeDTO> gradeHistory = toGradeHistory(report.getGrades());
        progress.setGradeHistory(gradeHistory);

        if (!gradeHistory.isEmpty() && gradeHistory.get(0).getClasse() != null) {
            progress.setClassId(gradeHistory.get(0).getClasse().getId());
        }

        Map<String, Double> gradesBySubject = gradeHistory.stream()
                .filter(grade -> grade.getSubject() != null && grade.getSubject().getNameSubject() != null)
                .collect(Collectors.groupingBy(
                        grade -> grade.getSubject().getNameSubject(),
                        Collectors.averagingDouble(grade -> grade.getGrade() != null ? grade.getGrade().doubleValue() : 0.0)));
        progress.setGradesBySubject(gradesBySubject);

        Map<String, List<Double>> progressBySubject = gradeHistory.stream()
                .filter(grade -> grade.getSubject() != null && grade.getSubject().getNameSubject() != null)
                .collect(Collectors.groupingBy(
                        grade -> grade.getSubject().getNameSubject(),
                        Collectors.mapping(
                                grade -> grade.getGrade() != null ? grade.getGrade().doubleValue() : 0.0,
                                Collectors.toList())));
        progress.setProgressBySubject(progressBySubject);

        return progress;
    }

    private List<GradeDTO> toGradeHistory(List<SubjectGradeDTO> subjectGrades) {
        if (subjectGrades == null) {
            return List.of();
        }
        return subjectGrades.stream()
                .map(subjectGrade -> GradeDTO.builder()
                        .id(subjectGrade.getId())
                        .subject(subjectGrade.getSubject())
                        .sequence(subjectGrade.getSequence())
                        .classe(subjectGrade.getClasse())
                        .trimestre(subjectGrade.getTrimestre())
                        .grade(subjectGrade.getGrade() != null ? subjectGrade.getGrade().getGrade() : BigDecimal.ZERO)
                        .assessmentDate(subjectGrade.getGrade() != null ? subjectGrade.getGrade().getAssessmentDate() : null)
                        .period(subjectGrade.getGrade() != null ? subjectGrade.getGrade().getPeriod() : null)
                        .build())
                .sorted(Comparator.comparing(GradeDTO::getAssessmentDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
}

// package com.school.management.service;

// import java.time.LocalDate;

// import com.school.exception.BusinessException;
// import com.school.management.dto.ClassPerformanceReport;
// import com.school.management.dto.PaymentSummaryReport;
// import com.school.management.dto.StudentReportDTO;

// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class ReportService {

//     private final StudentService studentService;
//     private final GradeService gradeService;
//     private final PaymentService paymentService;
//     private final PdfGenerationService pdfGenerationService;

//     public byte[] generateClassReport(Long classId, String period) {
//         try {
//             ClassPerformanceReport report = gradeService.generateClassePerformanceReport(classId, period);
//             return pdfGenerationService.generateClassPerformanceReport(report);
//         } catch (Exception e) {
//             throw new BusinessException("Erreur lors de la génération du rapport de classe", e);
//         }
//     }

//     public byte[] generatePaymentReport(LocalDate startDate, LocalDate endDate) {
//         try {
//             PaymentSummaryReport report = paymentService.generatePaymentSummaryReport(startDate, endDate);
//             return pdfGenerationService.generatePaymentSummaryReport(report);
//         } catch (Exception e) {
//             throw new BusinessException("Erreur lors de la génération du rapport de paiement", e);
//         }
//     }

//     public byte[] generateStudentProgressReport(Long studentId) {
//         try {
//             StudentReportDTO report = studentService.generateStudentReport(String.valueOf(studentId), null);
//             return pdfGenerationService.generateReportCard(report);
//         } catch (Exception e) {
//             throw new BusinessException("Erreur lors de la génération du bulletin de l'étudiant", e);
//         }
//     }


//     public byte[] generateStudentReport(String studentId, String period) {
//         try {
//             StudentReportDTO report = studentService.generateStudentReport(studentId, period);
//             return pdfGenerationService.generateReportCard(report);
//         } catch (Exception e) {
//             throw new BusinessException("Erreur lors de la génération du rapport de l'étudiant", e);
//         }
//     }
// }
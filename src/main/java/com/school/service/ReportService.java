// package com.school.service;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.cache.annotation.Cacheable;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.school.management.dto.GradeDTO;
// import com.school.management.dto.StudentDTO;
// import com.school.dto.*;
// import com.school.management.service.GradeService;
// import com.school.management.service.StudentService;
// import com.school.management.service.PaymentService;
// import com.school.management.service.PdfGenerationService;

// @Service
// public class ReportService {

//     private final StudentService studentService;
//     private final GradeService gradeService;
//     private final PaymentService paymentService;
//     private final PdfGenerationService pdfGenerationService;

//     public ReportService(StudentService studentService, GradeService gradeService,
//                         PaymentService paymentService, PdfGenerationService pdfGenerationService) {
//         this.studentService = studentService;
//         this.gradeService = gradeService;
//         this.paymentService = paymentService;
//         this.pdfGenerationService = pdfGenerationService;
//     }

//     @Cacheable(value = "reports", key = "'class_performance_' + #classId + '_' + #period")
//     public ClassPerformanceReport generateClassPerformanceReport(String classId, String period) {
//         ClassPerformanceReport report = new ClassPerformanceReport();
//         report.setClassId(Long.parseLong(classId));
//         report.setPeriod(period);

//         List<GradeDTO> grades = gradeService.findByClassIdAndPeriod(Long.parseLong(classId), period, Pageable.unpaged()).getContent();
        
//         Map<String, Double> subjectAverages = calculateSubjectAverages(grades);
//         report.setSubjectAverages(subjectAverages);
        
//         double successRate = calculateSuccessRate(grades);
//         report.setSuccessRate(successRate);
        
//         double classAverage = calculateClassAverage(grades);
//         report.setClassAverage(classAverage);
        
//         report.setHighestAverage(Double.valueOf(findHighestAverage(grades)));
//         report.setLowestAverage(Double.valueOf(findLowestAverage(grades)));

//         return report;
//     }

//     @Cacheable(value = "reports", key = "'student_progress_' + #studentId")
//     public StudentProgressReport generateStudentProgressReport(String studentId) {
//         StudentProgressReport report = new StudentProgressReport();
//         report.setStudentId(studentId);

//         StudentDTO student = studentService.getStudentById(Long.parseLong(studentId));
//         report.setStudent(student);

//         List<GradeDTO> grades = gradeService.findByStudentId(Long.parseLong(studentId), Pageable.unpaged()).getContent();
//         List<com.school.dto.GradeDTO> convertedGrades = grades.stream()
//             .map(this::convertGradeDTO)
//             .collect(Collectors.toList());
//         report.setGradeHistory(convertedGrades);

//         Map<String, List<Double>> progressBySubject = calculateProgressBySubject(grades);
//         report.setProgressBySubject(progressBySubject);

//         return report;
//     }

//     @Cacheable(value = "reports", key = "'payment_summary_' + #startDate + '_' + #endDate")
//     public PaymentSummaryReport generatePaymentSummaryReport(LocalDate startDate, LocalDate endDate) {
//         PaymentSummaryReport report = new PaymentSummaryReport();
//         report.setStartDate(startDate);
//         report.setEndDate(endDate);

//         List<PaymentDTO> payments = paymentService.findByDateRange(startDate, endDate);
        
//         report.setTotalAmount(calculateTotalAmount(payments));
//         report.setPaymentsByType(groupPaymentsByType(payments));
//         report.setPaymentsByMonth(groupPaymentsByMonth(payments));
//         report.setUnpaidFees(calculateUnpaidFees(startDate, endDate));

//         return report;
//     }

//     private Map<String, Double> calculateSubjectAverages(List<GradeDTO> grades) {
//         return grades.stream()
//                 .collect(Collectors.groupingBy(
//                     GradeDTO::getSubject,
//                     Collectors.averagingDouble(GradeDTO::getValue)
//                 ));
//     }

//     private double calculateSuccessRate(List<GradeDTO> grades) {
//         long passedCount = grades.stream()
//                 .filter(g -> g.getValue() >= 10.0)
//                 .count();
//         return grades.isEmpty() ? 0.0 : (double) passedCount / grades.size() * 100;
//     }

//     private double calculateClassAverage(List<GradeDTO> grades) {
//         return grades.stream()
//                 .mapToDouble(GradeDTO::getValue)
//                 .average()
//                 .orElse(0.0);
//     }

//     // private double findHighestAverage(List<GradeDTO> grades) {
//     //     return grades.stream()
//     //             .mapToDouble(GradeDTO::getValue)
//     //             .max()
//     //             .orElse(0.0);
//     // }

//     // private double findLowestAverage(List<GradeDTO> grades) {
//     //     return grades.stream()
//     //             .mapToDouble(GradeDTO::getValue)
//     //             .min()
//     //             .orElse(0.0);
//     // }

//     private BigDecimal calculateTotalAmount(List<PaymentDTO> payments) {
//         return payments.stream()
//                 .map(PaymentDTO::getAmount)
//                 .reduce(BigDecimal.ZERO, BigDecimal::add);
//     }

//     private Map<String, BigDecimal> groupPaymentsByType(List<PaymentDTO> payments) {
//         return payments.stream()
//                 .collect(Collectors.groupingBy(
//                     PaymentDTO::getType,
//                     Collectors.reducing(
//                         BigDecimal.ZERO,
//                         PaymentDTO::getAmount,
//                         BigDecimal::add
//                     )
//                 ));
//     }

//     private Map<String, BigDecimal> groupPaymentsByMonth(List<PaymentDTO> payments) {
//         return payments.stream()
//                 .collect(Collectors.groupingBy(
//                     p -> p.getPaymentDate().getMonth().toString(),
//                     Collectors.reducing(
//                         BigDecimal.ZERO,
//                         PaymentDTO::getAmount,
//                         BigDecimal::add
//                     )
//                 ));
//     }

//     private Map<String, BigDecimal> calculateUnpaidFees(LocalDate startDate, LocalDate endDate) {
//         // Cette méthode nécessite une logique spécifique à implémenter selon vos besoins
//         return Map.of();
//     }

//     // private Map<String, List<Double>> calculateProgressBySubject(List<GradeDTO> grades) {
//     //     return grades.stream()
//     //             .collect(Collectors.groupingBy(
//     //                 GradeDTO::getSubject,
//     //                 Collectors.mapping(
//     //                     GradeDTO::getGrade,
//     //                     Collectors.toList()
//     //                 )
//     //             ));
//     // }

//     @Transactional(readOnly = true)
//     public byte[] exportClassPerformanceReport(String classId, String period) throws Exception {
//         ClassPerformanceReport report = generateClassPerformanceReport(classId, period);
//         return pdfGenerationService.generateClassPerformanceReport(report);
//     }

//     @Transactional(readOnly = true)
//     public byte[] exportStudentProgressReport(String studentId) throws Exception {
//         StudentProgressReport report = generateStudentProgressReport(studentId);
//         return pdfGenerationService.generateClassPerformanceReport(convertToClassPerformanceReport(report));
//     }

//     @Transactional(readOnly = true)
//     public byte[] exportPaymentSummaryReport(LocalDate startDate, LocalDate endDate) throws Exception {
//         PaymentSummaryReport report = generatePaymentSummaryReport(startDate, endDate);
//         return pdfGenerationService.generatePaymentReceipt(convertToPaymentDTO(report));
//     }

//     private ClassPerformanceReport convertToClassPerformanceReport(StudentProgressReport report) {
//         ClassPerformanceReport classReport = new ClassPerformanceReport();
//         classReport.setClassId(report.getClassId());
//         // Conversion des données nécessaires
//         return classReport;
//     }

//     private PaymentDTO convertToPaymentDTO(PaymentSummaryReport report) {
//         PaymentDTO payment = new PaymentDTO();
//         payment.setAmount(report.getTotalAmount());
//         // Conversion des données nécessaires
//         return payment;
//     }

//     private com.school.dto.GradeDTO convertGradeDTO(com.school.management.dto.GradeDTO grade) {
//         com.school.dto.GradeDTO convertedGrade = new com.school.dto.GradeDTO();
//         // Copy necessary fields from grade to convertedGrade
//         return convertedGrade;
//     }
// }

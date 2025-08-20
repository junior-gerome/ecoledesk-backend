// package com.school.service;

// import java.io.ByteArrayOutputStream;
// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.Map;

// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.springframework.stereotype.Service;

// import com.school.dto.ClassPerformanceReport;
// import com.school.management.dto.GradeDTO;
// import com.school.dto.PaymentSummaryReport;
// import com.school.dto.StudentProgressReport;
// import com.school.management.model.Grade;

// @Service
// public class ExcelExportService {

//     private final ReportService reportService;

//     public ExcelExportService(ReportService reportService) {
//         this.reportService = reportService;
//     }

//     public byte[] exportClassPerformanceToExcel(String classId, String period) throws Exception {
//         ClassPerformanceReport report = reportService.generateClassPerformanceReport(classId, period);

//         try (Workbook workbook = new XSSFWorkbook()) {
//             Sheet sheet = workbook.createSheet("Performance de la classe");

//             CellStyle headerStyle = createHeaderStyle(workbook);

//             Row headerRow = sheet.createRow(0);
//             createHeaderCell(headerRow, 0, "Matière", headerStyle);
//             createHeaderCell(headerRow, 1, "Moyenne", headerStyle);
//             createHeaderCell(headerRow, 2, "Taux de réussite", headerStyle);

//             int rowNum = 1;
//             for (Map.Entry<String, Double> entry : report.getSubjectAverages().entrySet()) {
//                 Row row = sheet.createRow(rowNum++);
//                 row.createCell(0).setCellValue(entry.getKey());
//                 row.createCell(1).setCellValue(entry.getValue());
//             }

//             rowNum += 2;
//             Row statsRow = sheet.createRow(rowNum++);
//             statsRow.createCell(0).setCellValue("Moyenne générale");
//             statsRow.createCell(1).setCellValue(report.getClassAverage());

//             Row successRow = sheet.createRow(rowNum++);
//             successRow.createCell(0).setCellValue("Taux de réussite global");
//             successRow.createCell(1).setCellValue(report.getClassSuccessRate());

//             for (int i = 0; i < 3; i++) {
//                 sheet.autoSizeColumn(i);
//             }

//             return workbookToBytes(workbook);
//         }
//     }

//     public byte[] exportStudentProgressToExcel(String studentId) throws Exception {
//         StudentProgressReport report = reportService.generateStudentProgressReport(studentId);

//         try (Workbook workbook = new XSSFWorkbook()) {
//             Sheet sheet = workbook.createSheet("Progression de l'élève");

//             CellStyle headerStyle = createHeaderStyle(workbook);

//             Row studentRow = sheet.createRow(0);
//             studentRow.createCell(0).setCellValue("Élève:");
//             studentRow.createCell(1).setCellValue(report.getStudentName());

//             Row headerRow = sheet.createRow(2);
//             createHeaderCell(headerRow, 0, "Matière", headerStyle);
//             createHeaderCell(headerRow, 1, "Date", headerStyle);
//             createHeaderCell(headerRow, 2, "Note", headerStyle);
//             createHeaderCell(headerRow, 3, "Progression", headerStyle); // À adapter selon les données réelles

//             int rowNum = 3;
//             for (Map.Entry<String, List<Double>> entry : report.getProgressBySubject().entrySet()) {
//                 Row row = sheet.createRow(rowNum++);
//                 row.createCell(0).setCellValue(entry.getKey());

//                 List<Double> grades = entry.getValue();
//                 for (int i = 0; i < grades.size(); i++) {
//                     row.createCell(i + 1).setCellValue(grades.get(i));
//                 }
//             }

//             for (int i = 0; i < 4; i++) {
//                 sheet.autoSizeColumn(i);
//             }

//             return workbookToBytes(workbook);
//         }
//     }

//     public byte[] exportPaymentSummaryToExcel(LocalDate startDate, LocalDate endDate) throws Exception {
//         PaymentSummaryReport report = reportService.generatePaymentSummaryReport(startDate, endDate);

//         try (Workbook workbook = new XSSFWorkbook()) {
//             Sheet sheet = workbook.createSheet("Synthèse des paiements");

//             CellStyle headerStyle = createHeaderStyle(workbook);

//             Row periodRow = sheet.createRow(0);
//             periodRow.createCell(0).setCellValue("Période du:");
//             periodRow.createCell(1).setCellValue(startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//             periodRow.createCell(2).setCellValue("au");
//             periodRow.createCell(3).setCellValue(endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

//             Row headerRow = sheet.createRow(2);
//             createHeaderCell(headerRow, 0, "Type", headerStyle);
//             createHeaderCell(headerRow, 1, "Montant", headerStyle);
//             createHeaderCell(headerRow, 2, "Statut", headerStyle); // Optionnel selon les données

//             int rowNum = 3;
//             for (Map.Entry<String, BigDecimal> entry : report.getPaymentsByType().entrySet()) {
//                 Row row = sheet.createRow(rowNum++);
//                 row.createCell(0).setCellValue(entry.getKey());
//                 row.createCell(1).setCellValue(entry.getValue().doubleValue());
//             }

//             Row totalRow = sheet.createRow(++rowNum);
//             totalRow.createCell(0).setCellValue("TOTAL");
//             totalRow.createCell(1).setCellValue(report.getTotalAmount().doubleValue());

//             for (int i = 0; i < 3; i++) {
//                 sheet.autoSizeColumn(i);
//             }

//             return workbookToBytes(workbook);
//         }
//     }

//     private CellStyle createHeaderStyle(Workbook workbook) {
//         CellStyle style = workbook.createCellStyle();
//         Font font = workbook.createFont();
//         font.setBold(true);
//         style.setFont(font);
//         style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//         style.setBorderBottom(BorderStyle.THIN);
//         style.setBorderTop(BorderStyle.THIN);
//         style.setBorderRight(BorderStyle.THIN);
//         style.setBorderLeft(BorderStyle.THIN);
//         return style;
//     }

//     private void createHeaderCell(Row row, int column, String value, CellStyle style) {
//         Cell cell = row.createCell(column);
//         cell.setCellValue(value);
//         cell.setCellStyle(style);
//     }

//     private byte[] workbookToBytes(Workbook workbook) throws Exception {
//         try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
//             workbook.write(bos);
//             return bos.toByteArray();
//         }
//     }

//     // Conversion GradeDTO vers Grade (selon la structure réelle)
//     private Grade convertGradeDTOToGrade(GradeDTO gradeDTO) {
//         Grade grade = new Grade();
//         grade.setGrade(gradeDTO.getGrade());
//         if (gradeDTO.getAssessmentDate() != null) {
//             grade.setCreatedAt(gradeDTO.getAssessmentDate());
//         }
//         return grade;
//     }

//     // Génération de feuille Excel avec les notes
//     private void addGradeData(Workbook workbook, Sheet sheet, List<GradeDTO> grades) {
//         Row headerRow = sheet.createRow(0);
//         headerRow.createCell(0).setCellValue("Date");
//         headerRow.createCell(1).setCellValue("Note");

//         int rowNum = 1;
//         for (GradeDTO grade : grades) {
//             Row row = sheet.createRow(rowNum++);
//             row.createCell(0).setCellValue(
//                 grade.getAssessmentDate() != null ?
//                 grade.getAssessmentDate().toLocalDate().toString() :
//                 "N/A"
//             );
//             row.createCell(1).setCellValue(
//                 grade.getGrade() != null ? grade.getGrade().doubleValue() : 0.0
//             );
//         }
//     }
// }

package com.school.platform.enrollment.application;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int EXPORT_BATCH_SIZE = 500;

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public byte[] generateStudentsExcel(String keyword) {
        List<StudentExportRow> rows = loadRows(keyword);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Eleves");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("Liste des eleves");
            title.getCell(0).setCellStyle(titleStyle);

            Row generatedAt = sheet.createRow(1);
            generatedAt.createCell(0).setCellValue("Genere le");
            generatedAt.createCell(1).setCellValue(LocalDate.now().format(DATE_FORMAT));

            Row header = sheet.createRow(3);
            String[] columns = {
                    "ID", "Nom", "Prenom", "Date de naissance", "Genre",
                    "Classe", "Section", "Telephone responsable legal"
            };
            for (int index = 0; index < columns.length; index++) {
                header.createCell(index).setCellValue(columns[index]);
                header.getCell(index).setCellStyle(headerStyle);
            }

            int rowIndex = 4;
            for (StudentExportRow row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.id());
                excelRow.createCell(1).setCellValue(row.lastName());
                excelRow.createCell(2).setCellValue(row.firstName());
                excelRow.createCell(3).setCellValue(row.birthDate());
                excelRow.createCell(4).setCellValue(row.gender());
                excelRow.createCell(5).setCellValue(row.className());
                excelRow.createCell(6).setCellValue(row.section());
                excelRow.createCell(7).setCellValue(row.guardianPhone());
            }

            for (int index = 0; index < columns.length; index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("Erreur lors de la generation Excel de la liste des eleves", ex);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateStudentsPdf(String keyword) {
        List<StudentExportRow> rows = loadRows(keyword);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(output);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Liste des eleves")
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Genere le " + LocalDate.now().format(DATE_FORMAT)));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[] { 8, 16, 16, 16, 10, 14, 12, 18 }))
                    .useAllAvailableWidth();
            List.of("ID", "Nom", "Prenom", "Naissance", "Genre", "Classe", "Section", "Tel. responsable legal")
                    .forEach(label -> table.addHeaderCell(headerCell(label)));

            if (rows.isEmpty()) {
                table.addCell(new Cell(1, 8).add(new Paragraph("Aucun eleve trouve")));
            } else {
                rows.forEach(row -> {
                    table.addCell(cell(String.valueOf(row.id())));
                    table.addCell(cell(row.lastName()));
                    table.addCell(cell(row.firstName()));
                    table.addCell(cell(row.birthDate()));
                    table.addCell(cell(row.gender()));
                    table.addCell(cell(row.className()));
                    table.addCell(cell(row.section()));
                    table.addCell(cell(row.guardianPhone()));
                });
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("Erreur lors de la generation PDF de la liste des eleves", ex);
        }
    }

    private List<StudentExportRow> loadRows(String keyword) {
        Map<Long, Enrollment> latestByStudent = enrollmentRepository.findByStatus(EnrollmentStatus.CONFIRMED)
                .stream()
                .filter(e -> e.getStudent() != null && e.getStudent().getId() != null)
                .collect(Collectors.toMap(
                        e -> e.getStudent().getId(),
                        e -> e,
                        (left, right) -> left.getEnrollmentDate() != null && right.getEnrollmentDate() != null
                                && left.getEnrollmentDate().isAfter(right.getEnrollmentDate()) ? left : right));

        String normalizedKeyword = normalize(keyword);

        List<StudentExportRow> rows = new ArrayList<>();
        int pageNumber = 0;
        Page<Student> page;
        do {
            page = studentRepository.findAll(PageRequest.of(pageNumber++, EXPORT_BATCH_SIZE));
            page.getContent().stream()
                    .map(student -> toExportRow(student, latestByStudent.get(student.getId())))
                    .filter(row -> normalizedKeyword.isBlank() || matches(row, normalizedKeyword))
                    .forEach(rows::add);
        } while (page.hasNext());

        return rows.stream()
                .sorted(Comparator
                        .comparing(StudentExportRow::lastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(StudentExportRow::firstName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private StudentExportRow toExportRow(Student student, Enrollment enrollment) {
        Guardian guardian = student.getGuardian();
        String className = enrollment != null && enrollment.getClassroom() != null
                ? safe(enrollment.getClassroom().getNameClasse())
                : "Non inscrit";
        String section = enrollment != null
                && enrollment.getClassroom() != null
                && enrollment.getClassroom().getSection() != null
                        ? safe(enrollment.getClassroom().getSection().getLibelle())
                        : "";

        return new StudentExportRow(
                student.getId() == null ? 0L : student.getId(),
                safe(student.getLastNameStudent()),
                safe(student.getFirstNameStudent()),
                student.getDateOfBirth() == null ? "" : student.getDateOfBirth().format(DATE_FORMAT),
                student.getGender() == null ? "" : student.getGender().name(),
                className,
                section,
                guardian == null || guardian.getPerson() == null ? "" : safe(guardian.getPerson().getPhone()));
    }

    private boolean matches(StudentExportRow row, String keyword) {
        return normalize(row.id()).contains(keyword)
                || normalize(row.lastName()).contains(keyword)
                || normalize(row.firstName()).contains(keyword)
                || normalize(row.className()).contains(keyword)
                || normalize(row.section()).contains(keyword)
                || normalize(row.guardianPhone()).contains(keyword);
    }

    private Cell headerCell(String text) {
        return cell(text).setBold().setTextAlignment(TextAlignment.CENTER);
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(safe(text)));
    }

    private String normalize(Object value) {
        return safe(String.valueOf(value)).toLowerCase(Locale.ROOT).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record StudentExportRow(
            Long id,
            String lastName,
            String firstName,
            String birthDate,
            String gender,
            String className,
            String section,
            String guardianPhone) {
    }
}

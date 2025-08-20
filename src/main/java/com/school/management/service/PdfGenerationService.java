package com.school.management.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.school.management.dto.ClassPerformanceReport;
import com.school.management.dto.PaymentSummaryReport;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.school.management.dto.GradeDTO;
import com.school.management.dto.PaymentDTO;
import com.school.management.dto.StudentProgressReport;
import com.school.management.dto.StudentReportDTO;
import com.school.management.dto.SubjectGradeDTO;
import com.school.service.ClassService;  // Import corrigé

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final ClassService classService;
    // EnseignantService non présent dans le code fourni - commenté temporairement
    // private final EnseignantService enseignantService;

    public byte[] generateReportCard(StudentReportDTO report) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addHeader(document);
            addStudentInfo(document, report);
            addGradesTable(document, convertToGradeDTOList(report.getGrades()));
            addSummary(document, report);
            addSignatures(document);

            document.close();
            return baos.toByteArray();
        }
    }

    public byte[] generatePaymentSummaryReport(PaymentSummaryReport report) {
        // TODO: Ajouter l'implémentation de la génération du résumé de paiements
        return new byte[0];
    }

    public byte[] generateClassPerformanceReport(ClassPerformanceReport report) {
        // TODO: Ajouter l'implémentation du rapport de performance de la classe
        return new byte[0];
    }

    public byte[] generatePaymentReceipt(PaymentDTO payment) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // TODO: Ajouter l'implémentation du reçu de paiement

            document.close();
            return baos.toByteArray();
        }
    }

    public byte[] generateClassReport(Long classId) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // TODO: Ajouter l'implémentation du rapport de classe

            document.close();
            return baos.toByteArray();
        }
    }

    public byte[] generateStudentReport(StudentProgressReport report) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // TODO: Ajouter l'implémentation du rapport de progression

            document.close();
            return baos.toByteArray();
        }
    }

    private List<GradeDTO> convertToGradeDTOList(List<SubjectGradeDTO> subjectGrades) {
        return subjectGrades.stream()
            .map(this::convertSubjectGradeToGradeDTO)
            .collect(Collectors.toList());
    }

    private GradeDTO convertSubjectGradeToGradeDTO(SubjectGradeDTO subjectGrade) {
        return GradeDTO.builder()
            .id(subjectGrade.getId())
            .subject(subjectGrade.getSubject())
            .grade(subjectGrade.getGrade() != null ? subjectGrade.getGrade().getGrade() : BigDecimal.ZERO)
            .classe(subjectGrade.getClasse())
            .sequence(subjectGrade.getSequence())
            .trimestre(subjectGrade.getTrimestre())
            .build();
    }

    private void addHeader(Document document) {
        Paragraph header = new Paragraph()
            .setTextAlignment(TextAlignment.CENTER)
            .setBold();

        header.add("RÉPUBLIQUE DU CAMEROUN\n")
              .add("MINISTÈRE DE L'ÉDUCATION DE BASE\n")
              .add("DÉLÉGATION RÉGIONALE DE L'ÉDUCATION DE BASE DU LITTORAL\n")
              .add("DÉLÉGATION DÉPARTEMENTALE DE L'ÉDUCATION DE BASE DU WOURI\n")
              .add("DÉLÉGATION D'ARRONDISSEMENT DE L'ÉDUCATION DE BASE\n")
              .add("GROUPE SCOLAIRE BILINGUE LA PATIENCE\n\n");

        document.add(header);
    }

    private void addStudentInfo(Document document, StudentReportDTO report) {
        Table infoTable = new Table(2).setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(createCell("N° MATRICULE: " + report.getId()));
        infoTable.addCell(createCell("CLASSE: " + report.getNameClasse()));
        infoTable.addCell(createCell("NOM DE L'ÉLÈVE: " + report.getLastNameStudent()));
        infoTable.addCell(createCell("PRÉNOM DE L'ÉLÈVE: " + report.getFirstNameStudent()));
        infoTable.addCell(createCell("ENSEIGNANT: " + report.getLastnameTeacher() +""+ report.getFirstnameteacher()));

        document.add(infoTable);
    }

    private void addGradesTable(Document document, List<GradeDTO> grades) {
        Table table = new Table(5).setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createHeaderCell("DISCIPLINE"));
        table.addHeaderCell(createHeaderCell("SÉQUENCE 1"));
        table.addHeaderCell(createHeaderCell("SÉQUENCE 2"));
        table.addHeaderCell(createHeaderCell("TRIMESTRE"));
        table.addHeaderCell(createHeaderCell("APPRÉCIATION"));

        for (GradeDTO grade : grades) {
            table.addCell(createCell(grade.getSubject().getNameSubject()));
            table.addCell(createCell(formatNote(grade.getGrade())));
            table.addCell(createCell("-")); // TODO: Ajouter la note de séquence 2
            table.addCell(createCell("-")); // TODO: Ajouter la moyenne du trimestre
            table.addCell(createCell("-")); // TODO: Ajouter une appréciation personnalisée
        }

        document.add(table);
    }

    private void addSummary(Document document, StudentReportDTO report) {
        Table summaryTable = new Table(2).setWidth(UnitValue.createPercentValue(100));

        summaryTable.addCell(createCell("MOYENNE OBTENUE : " + formatNote(BigDecimal.valueOf(report.getAverageGrade())) + " / 20"));
        summaryTable.addCell(createCell("RANG : " + report.getRank() + " / " + report.getTotalStudents()));

        document.add(summaryTable);
    }

    private void addSignatures(Document document) {
        Table signatureTable = new Table(3).setWidth(UnitValue.createPercentValue(100));

        signatureTable.addCell(createCell("Le Parent"));
        signatureTable.addCell(createCell("L'Enseignant"));
        signatureTable.addCell(createCell("Le Directeur"));

        document.add(signatureTable);
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
            .add(new Paragraph(text))
            .setTextAlignment(TextAlignment.CENTER)
            .setBold();
    }

    private Cell createCell(String text) {
        return new Cell()
            .add(new Paragraph(text != null ? text : ""))
            .setTextAlignment(TextAlignment.CENTER);
    }

    private String formatNote(BigDecimal note) {
        return note != null ? String.format("%.2f", note) : "0.00";
    }
}
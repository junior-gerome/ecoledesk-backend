package com.school.platform.reporting.application;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.school.platform.academic.application.interfaces.ClassRoomService;
import com.school.platform.reporting.application.dto.ClassPerformanceReport;
import com.school.platform.academic.application.dto.classeroom.ClasseRoomDTO;
import com.school.platform.academic.application.dto.grade.GradeDTO;
import com.school.platform.billing.application.dto.PaymentDTO;
import com.school.platform.billing.application.dto.PaymentSummaryReport;
import com.school.platform.reporting.application.dto.StudentProgressReport;
import com.school.platform.reporting.application.dto.StudentReportDTO;
import com.school.platform.reporting.application.dto.SubjectGradeDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final ClassRoomService classRoomService;

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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Rapport de paiements").setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(
                    "Periode: " + safeDate(report.getStartDate()) + " au " + safeDate(report.getEndDate())));
            document.add(new Paragraph("Total encaisse: " + formatAmount(report.getTotalAmount())));
            document.add(new Paragraph("Nombre total de paiements: " + report.getTotalPayments()));
            document.add(new Paragraph(" "));

            addAmountMapTable(document, "Montant par type de paiement", report.getAmountByType());
            addAmountMapTable(document, "Montant par mois", report.getAmountByMonth());
            addAmountMapTable(document, "Montant par classe", report.getAmountByClass());
            addAmountMapTable(document, "Restes a payer par classe", report.getUnpaidFees());

            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Erreur lors de la generation du PDF de paiements", ex);
        }
    }

    public byte[] generateClassPerformanceReport(ClassPerformanceReport report) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Rapport de performance de classe").setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Classe: " + safeText(report.getNameClasse())));
            document.add(new Paragraph("Periode: " + safeText(report.getPeriod())));
            document.add(new Paragraph("Moyenne generale: " + formatDouble(report.getClassAverage())));
            document.add(new Paragraph("Taux de reussite: " + formatDouble(report.getSuccessRate()) + "%"));
            document.add(new Paragraph("Eleves admis: " + report.getPassingStudents() + " / " + report.getTotalStudents()));
            document.add(new Paragraph("Meilleure moyenne: " + formatDouble(report.getHighestAverage())));
            document.add(new Paragraph("Moyenne la plus basse: " + formatDouble(report.getLowestAverage())));
            document.add(new Paragraph(" "));

            addDoubleMapTable(document, "Moyennes par matiere", report.getSubjectAverages());
            addIntegerMapTable(document, "Distribution des notes", report.getGradeDistribution());

            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Erreur lors de la generation du PDF de performance", ex);
        }
    }

    public byte[] generatePaymentReceipt(PaymentDTO payment) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Recu de paiement").setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Reference: #" + (payment.getId() != null ? payment.getId() : "N/A")));
            document.add(new Paragraph("Date: " + safeDate(payment.getDatePaiement())));
            document.add(new Paragraph("Eleve: " + studentName(payment)));
            document.add(new Paragraph("Montant paye: " + formatAmount(toBigDecimal(payment.getMontantPaye()))));
            document.add(new Paragraph("Reste a payer: " + formatAmount(toBigDecimal(payment.getMontantRestant()))));
            document.add(new Paragraph("Type: " + (payment.getTypePaiement() != null ? payment.getTypePaiement().name() : "N/A")));
            document.add(new Paragraph("Description: " + safeText(payment.getDescription())));

            document.close();
            return baos.toByteArray();
        }
    }

    public byte[] generateClassReport(Long classId) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            ClasseRoomDTO classe = classRoomService.getClassRoomById(classId);

            document.add(new Paragraph("Fiche de classe").setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Classe: " + safeText(classe.getNameClasse())));
            document.add(new Paragraph("Niveau: " + safeText(classe.getLevel())));
            document.add(new Paragraph("Section: " + (classe.getSection() != null ? safeText(classe.getSection().getLibelle()) : "N/A")));
            document.add(new Paragraph("Capacite: " + (classe.getCapacity() != null ? classe.getCapacity() : 0)));
            document.add(new Paragraph("Description: " + safeText(classe.getDescription())));

            document.close();
            return baos.toByteArray();
        }
    }

    public byte[] generateStudentReport(StudentProgressReport report) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Rapport de progression eleve").setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Eleve: " + studentName(report)));
            document.add(new Paragraph("Classe: " + safeText(report.getClassLevel())));
            document.add(new Paragraph("Periode: " + safeText(report.getPeriod())));
            document.add(new Paragraph("Moyenne: " + formatDouble(report.getAverageGrade())));
            document.add(new Paragraph("Rang: " + report.getRank() + " / " + report.getTotalStudents()));
            document.add(new Paragraph("Absences: " + report.getAbsenceDays() + " jour(s)"));
            document.add(new Paragraph("Enseignant: " + safeText(report.getLastnameTeacher())));
            document.add(new Paragraph(" "));

            addDoubleMapTable(document, "Moyennes par matiere", report.getGradesBySubject());
            addProgressTable(document, report.getProgressBySubject());

            document.close();
            return baos.toByteArray();
        }
    }

    private List<GradeDTO> convertToGradeDTOList(List<SubjectGradeDTO> subjectGrades) {
        if (subjectGrades == null) {
            return List.of();
        }
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

        header.add("REPUBLIQUE DU CAMEROUN\n")
                .add("MINISTERE DE L'EDUCATION DE BASE\n")
                .add("GROUPE SCOLAIRE BILINGUE LA PATIENCE\n\n");

        document.add(header);
    }

    private void addStudentInfo(Document document, StudentReportDTO report) {
        Table infoTable = new Table(2).setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(createCell("Matricule: " + report.getId()));
        infoTable.addCell(createCell("Classe: " + safeText(report.getNameClasse())));
        infoTable.addCell(createCell("Nom: " + safeText(report.getLastNameStudent())));
        infoTable.addCell(createCell("Prenom: " + safeText(report.getFirstNameStudent())));
        infoTable.addCell(createCell(
                "Enseignant: " + safeText(report.getLastnameTeacher()) + " " + safeText(report.getFirstnameteacher())));
        infoTable.addCell(createCell("Periode: " + safeText(report.getPeriod())));

        document.add(infoTable);
    }

    private void addGradesTable(Document document, List<GradeDTO> grades) {
        Table table = new Table(5).setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createHeaderCell("Discipline"));
        table.addHeaderCell(createHeaderCell("Sequence 1"));
        table.addHeaderCell(createHeaderCell("Sequence 2"));
        table.addHeaderCell(createHeaderCell("Trimestre"));
        table.addHeaderCell(createHeaderCell("Appreciation"));

        for (GradeDTO grade : grades) {
            BigDecimal value = grade.getGrade() != null ? grade.getGrade() : BigDecimal.ZERO;
            table.addCell(createCell(grade.getSubject() != null ? safeText(grade.getSubject().getNameSubject()) : "N/A"));
            table.addCell(createCell(formatNote(value)));
            table.addCell(createCell(formatNote(value)));
            table.addCell(createCell(formatNote(value)));
            table.addCell(createCell(appreciationFor(value)));
        }

        document.add(table);
    }

    private void addSummary(Document document, StudentReportDTO report) {
        Table summaryTable = new Table(2).setWidth(UnitValue.createPercentValue(100));

        summaryTable.addCell(createCell("Moyenne: " + formatDouble(report.getAverageGrade()) + " / 20"));
        summaryTable.addCell(createCell("Rang: " + report.getRank() + " / " + report.getTotalStudents()));
        summaryTable.addCell(createCell("Absences: " + report.getAbsenceDays()));
        summaryTable.addCell(createCell("Commentaire: " + safeText(report.getTeacherComments())));

        document.add(summaryTable);
    }

    private void addSignatures(Document document) {
        Table signatureTable = new Table(3).setWidth(UnitValue.createPercentValue(100));

        signatureTable.addCell(createCell("Responsable legal"));
        signatureTable.addCell(createCell("Enseignant"));
        signatureTable.addCell(createCell("Directeur"));

        document.add(signatureTable);
    }

    private void addAmountMapTable(Document document, String title, Map<String, BigDecimal> data) {
        document.add(new Paragraph(title).setBold());
        Table table = new Table(2).setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(createHeaderCell("Categorie"));
        table.addHeaderCell(createHeaderCell("Montant"));
        if (data == null || data.isEmpty()) {
            table.addCell(createCell("Aucune donnee"));
            table.addCell(createCell("0"));
        } else {
            data.forEach((key, value) -> {
                table.addCell(createCell(safeText(key)));
                table.addCell(createCell(formatAmount(value)));
            });
        }
        document.add(table);
    }

    private void addDoubleMapTable(Document document, String title, Map<String, Double> data) {
        document.add(new Paragraph(title).setBold());
        Table table = new Table(2).setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(createHeaderCell("Element"));
        table.addHeaderCell(createHeaderCell("Valeur"));
        if (data == null || data.isEmpty()) {
            table.addCell(createCell("Aucune donnee"));
            table.addCell(createCell("0.00"));
        } else {
            data.forEach((key, value) -> {
                table.addCell(createCell(safeText(key)));
                table.addCell(createCell(formatDouble(value)));
            });
        }
        document.add(table);
    }

    private void addIntegerMapTable(Document document, String title, Map<String, Integer> data) {
        document.add(new Paragraph(title).setBold());
        Table table = new Table(2).setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(createHeaderCell("Element"));
        table.addHeaderCell(createHeaderCell("Nombre"));
        if (data == null || data.isEmpty()) {
            table.addCell(createCell("Aucune donnee"));
            table.addCell(createCell("0"));
        } else {
            data.forEach((key, value) -> {
                table.addCell(createCell(safeText(key)));
                table.addCell(createCell(String.valueOf(value)));
            });
        }
        document.add(table);
    }

    private void addProgressTable(Document document, Map<String, List<Double>> progressBySubject) {
        document.add(new Paragraph("Progression par matiere").setBold());
        Table table = new Table(2).setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(createHeaderCell("Matiere"));
        table.addHeaderCell(createHeaderCell("Evolution"));
        if (progressBySubject == null || progressBySubject.isEmpty()) {
            table.addCell(createCell("Aucune donnee"));
            table.addCell(createCell("-"));
        } else {
            progressBySubject.forEach((subject, values) -> {
                String progression = values == null ? "-" : values.stream()
                        .map(this::formatDouble)
                        .collect(Collectors.joining(" -> "));
                table.addCell(createCell(safeText(subject)));
                table.addCell(createCell(progression));
            });
        }
        document.add(table);
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

    private String formatDouble(Double value) {
        return value != null ? String.format("%.2f", value) : "0.00";
    }

    private String formatAmount(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).toPlainString() + " FCFA";
    }

    private String safeText(String text) {
        return text != null ? text : "N/A";
    }

    private String safeDate(Object date) {
        return date != null ? date.toString() : "N/A";
    }

    private BigDecimal toBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String appreciationFor(BigDecimal note) {
        if (note == null) {
            return "N/A";
        }
        double value = note.doubleValue();
        if (value >= 16) {
            return "Excellent";
        }
        if (value >= 14) {
            return "Tres bien";
        }
        if (value >= 12) {
            return "Bien";
        }
        if (value >= 10) {
            return "Passable";
        }
        return "Insuffisant";
    }

    private String studentName(PaymentDTO payment) {
        if (payment.getStudent() == null) {
            return "N/A";
        }
        return safeText(payment.getStudent().getLastNameStudent()) + " "
                + safeText(payment.getStudent().getFirstNameStudent());
    }

    private String studentName(StudentProgressReport report) {
        if (report.getStudent() == null) {
            return "N/A";
        }
        return safeText(report.getStudent().getLastNameStudent()) + " "
                + safeText(report.getStudent().getFirstNameStudent());
    }
}

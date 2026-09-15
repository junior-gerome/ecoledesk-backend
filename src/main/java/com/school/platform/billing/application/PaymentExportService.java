package com.school.platform.billing.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.billing.application.dto.PaymentExportRequest;
import com.school.platform.billing.application.dto.PaymentExportRow;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;
import com.school.platform.settings.application.SettingsService;
import com.school.platform.settings.application.dto.AppPreferencesDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentExportService {

    private static final Logger log = LoggerFactory.getLogger(PaymentExportService.class);
    private static final DeviceRgb ACCENT = new DeviceRgb(31, 78, 121);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(31, 78, 121);
    private static final DeviceRgb ALT_ROW = new DeviceRgb(242, 242, 242);
    private static final DeviceRgb TOTAL_BG = new DeviceRgb(234, 240, 248);
    private static final XSSFColor FONT_WHITE = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null);
    private static final XSSFColor FILL_ACCENT = new XSSFColor(new byte[]{(byte) 31, (byte) 78, (byte) 121}, null);
    private static final XSSFColor FONT_GRAY_META = new XSSFColor(new byte[]{(byte) 89, (byte) 89, (byte) 89}, null);
    private static final XSSFColor FILL_ALT = new XSSFColor(new byte[]{(byte) 217, (byte) 217, (byte) 217}, null);
    private static final XSSFColor FILL_TOTAL = new XSSFColor(new byte[]{(byte) 234, (byte) 240, (byte) 248}, null);
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final PaiementRepository paiementRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SettingsService settingsService;

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    public record ExportContext(
            AcademicYear year,
            List<PaymentExportRow> rows,
            String schoolName,
            String filtersDescription,
            String generatedBy,
            LocalDateTime generatedAt) {
    }

    public ExportContext resolveExportContext(PaymentExportRequest request, String generatedBy) {
        AcademicYear year = resolveAndValidateYear(request);
        List<PaymentExportRow> rows = resolveRows(request);
        AppPreferencesDTO prefs = settingsService.getPreferences();
        return new ExportContext(
                year,
                rows,
                prefs.schoolName() != null && !prefs.schoolName().isBlank()
                        ? prefs.schoolName()
                        : "GROUPE SCOLAIRE BILINGUE LA PATIENCE",
                buildFiltersDescription(request, year),
                generatedBy,
                LocalDateTime.now());
    }

    public byte[] generateExcel(ExportContext ctx) {
        long started = System.nanoTime();
        log.info("EXPORT_START type=EXCEL generatedBy={}", ctx.generatedBy());
        try {
            byte[] content = buildExcel(ctx);
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            log.info("EXPORT_SUCCESS type=EXCEL rows={} durationMs={}", ctx.rows().size(), elapsed);
            return content;
        } catch (IOException ex) {
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            log.error("EXPORT_FAILURE type=EXCEL durationMs={}", elapsed, ex);
            throw new UncheckedIOException(ex);
        } catch (RuntimeException ex) {
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            log.error("EXPORT_FAILURE type=EXCEL durationMs={}", elapsed, ex);
            throw ex;
        }
    }

    public byte[] generatePdf(ExportContext ctx) {
        long started = System.nanoTime();
        log.info("EXPORT_START type=PDF generatedBy={}", ctx.generatedBy());
        try {
            byte[] content = buildPdf(ctx);
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            log.info("EXPORT_SUCCESS type=PDF rows={} durationMs={}", ctx.rows().size(), elapsed);
            return content;
        } catch (IOException ex) {
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            log.error("EXPORT_FAILURE type=PDF durationMs={}", elapsed, ex);
            throw new UncheckedIOException(ex);
        } catch (RuntimeException ex) {
            long elapsed = (System.nanoTime() - started) / 1_000_000;
            log.error("EXPORT_FAILURE type=PDF durationMs={}", elapsed, ex);
            throw ex;
        }
    }

    public String exportFileName(ExportContext ctx, String extension) {
        String yearSlug = sanitizeFileName(safe(ctx.year().getLibelleAcademicYear()));
        String dateSlug = LocalDate.now().format(YYYY_MM_DD);
        return String.format("paiements-%s-%s.%s", yearSlug, dateSlug, extension);
    }

    // ---------------------------------------------------------------------------
    // Context helpers
    // ---------------------------------------------------------------------------

    private AcademicYear resolveAndValidateYear(PaymentExportRequest request) {
        if (request.academicYearId() == null) {
            return academicYearRepository.findByStatutCode(true)
                    .orElseThrow(() -> new com.school.platform.shared.domain.exception.BadRequestException(
                            "Aucune annee scolaire active. Selectionnez une annee avant d'exporter."));
        }
        AcademicYear requested = academicYearRepository.findById(request.academicYearId())
                .orElseThrow(() -> new com.school.platform.shared.domain.exception.ResourceNotFoundException(
                        "Annee scolaire", "id", request.academicYearId()));
        if (!requested.isStatutCode()) {
            throw new com.school.platform.shared.domain.exception.BadRequestException(
                    "L'export est restreint a l'annee scolaire active uniquement.");
        }
        return requested;
    }

    private List<PaymentExportRow> resolveRows(PaymentExportRequest request) {
        PaymentExportRequest scoped = new PaymentExportRequest(
                request.studentId(),
                request.classroomId(),
                request.academicYearId(),
                request.q(),
                request.receiptNumber(),
                request.type(),
                request.status(),
                request.paymentMethod(),
                request.startDate(),
                request.endDate());
        return paiementRepository.searchExport(
                        scoped.studentId(),
                        scoped.classroomId(),
                        scoped.academicYearId(),
                        scoped.type(),
                        scoped.paymentMethod(),
                        scoped.startDate(),
                        scoped.endDate(),
                        scoped.q(),
                        scoped.receiptNumber())
                .stream()
                .map(p -> new PaymentExportRow(
                        p.getId(),
                        p.getReceiptNumber(),
                        fullName(p.getStudent()),
                        p.getStudent() != null ? p.getStudent().getStudentNumber() : null,
                        p.getEnrollment() != null && p.getEnrollment().getClassroom() != null
                                ? p.getEnrollment().getClassroom().getNameClasse()
                                : null,
                        p.getTypePaiement() != null ? p.getTypePaiement().name() : null,
                        nz(p.getMontantPaye()),
                        nz(p.getRemise()),
                        nz(p.getMontantRestant()),
                        paymentStatus(p),
                        p.getDatePaiement(),
                        p.getDueDate(),
                        p.getPaymentMethod(),
                        p.getDescription()))
                .filter(row -> scoped.status() == null
                        || scoped.status().isBlank()
                        || scoped.status().equalsIgnoreCase(row.status()))
                .toList();
    }

    private String buildFiltersDescription(PaymentExportRequest request, AcademicYear year) {
        StringBuilder desc = new StringBuilder();
        desc.append("Annee: ").append(safe(year.getLibelleAcademicYear()));
        if (request.studentId() != null) {
            desc.append(" | Eleve #").append(request.studentId());
        }
        if (request.classroomId() != null) {
            desc.append(" | Classe #").append(request.classroomId());
        }
        if (request.type() != null) {
            desc.append(" | Type: ").append(request.type().name());
        }
        if (request.status() != null && !request.status().isBlank()) {
            desc.append(" | Statut: ").append(request.status());
        }
        if (request.paymentMethod() != null && !request.paymentMethod().isBlank()) {
            desc.append(" | Methode: ").append(request.paymentMethod());
        }
        if (request.startDate() != null) {
            desc.append(" | Du ").append(request.startDate().format(DD_MM_YYYY));
        }
        if (request.endDate() != null) {
            desc.append(" au ").append(request.endDate().format(DD_MM_YYYY));
        }
        if (request.q() != null && !request.q().isBlank()) {
            desc.append(" | Recherche: ").append(request.q().trim());
        }
        if (request.receiptNumber() != null && !request.receiptNumber().isBlank()) {
            desc.append(" | Recu: ").append(request.receiptNumber().trim());
        }
        return desc.toString();
    }

    // ---------------------------------------------------------------------------
    // Excel (SXSSF)
    // ---------------------------------------------------------------------------

    private byte[] buildExcel(ExportContext ctx) throws IOException {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            CellStyle titleStyle = buildTitleStyle(wb);
            CellStyle subtitleStyle = buildSubtitleStyle(wb);
            CellStyle metaStyle = buildMetaStyle(wb);
            CellStyle headerStyle = buildExcelHeaderStyle(wb);
            CellStyle bodyStyle = buildBodyStyle(wb);
            CellStyle moneyStyle = buildMoneyStyle(wb);
            CellStyle dateStyle = buildDateStyle(wb);
            CellStyle totalStyle = buildTotalStyle(wb);

            Sheet sheet = wb.createSheet("Paiements");

            int rowIdx = 0;

            // Row 0: Title
            mergeRow(sheet, rowIdx, 11);
            Row row0 = sheet.createRow(rowIdx++);
            row0.setHeightInPoints(24);
            Cell titleCell = row0.createCell(0);
            titleCell.setCellValue("LISTE DES PAIEMENTS");
            titleCell.setCellStyle(titleStyle);

            // Row 1: School name + year
            mergeRow(sheet, rowIdx, 11);
            Row row1 = sheet.createRow(rowIdx++);
            Cell subCell = row1.createCell(0);
            subCell.setCellValue(safe(ctx.schoolName()) + " - Annee scolaire " + safe(ctx.year().getLibelleAcademicYear()));
            subCell.setCellStyle(subtitleStyle);

            // Row 2: Metadata
            mergeRow(sheet, rowIdx, 11);
            Row row2 = sheet.createRow(rowIdx++);
            Cell metaCell = row2.createCell(0);
            metaCell.setCellValue("Genere le " + ctx.generatedAt().format(DD_MM_YYYY) + " a " + ctx.generatedAt().format(HH_MM) + " par " + safe(ctx.generatedBy()));
            metaCell.setCellStyle(metaStyle);

            // Row 3: Filters
            mergeRow(sheet, rowIdx, 11);
            Row row3 = sheet.createRow(rowIdx++);
            Cell filterCell = row3.createCell(0);
            filterCell.setCellValue("Filtres: " + safe(ctx.filtersDescription()));
            filterCell.setCellStyle(metaStyle);

            // Row 4: blank
            sheet.createRow(rowIdx++);

            // Row 5: Headers
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"No", "Recu", "Eleve", "Classe", "Type", "Date", "Montant (XOF)", "Remise (XOF)", "Restant (XOF)", "Methode", "Statut", "Description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int firstDataRow = rowIdx;
            BigDecimal sumPaid = BigDecimal.ZERO;
            BigDecimal sumDiscount = BigDecimal.ZERO;
            BigDecimal sumRemaining = BigDecimal.ZERO;

            // Data rows
            List<PaymentExportRow> rows = ctx.rows();
            for (PaymentExportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                boolean alt = (rowIdx - firstDataRow) % 2 == 0;
                CellStyle rowStyle = alt ? buildAltBodyStyle(wb) : bodyStyle;

                setCell(row, 0, r.id() != null ? r.id().toString() : "", rowStyle);
                setCell(row, 1, safe(r.receiptNumber()), rowStyle);
                setCell(row, 2, safe(r.studentName()), rowStyle);
                setCell(row, 3, safe(r.className()), rowStyle);
                setCell(row, 4, safe(r.type()), rowStyle);
                if (r.paymentDate() != null) {
                    setDateCell(row, 5, r.paymentDate(), dateStyle);
                } else {
                    setCell(row, 5, "", rowStyle);
                }
                setMoneyCell(row, 6, nz(r.amount()), moneyStyle);
                setMoneyCell(row, 7, nz(r.discount()), moneyStyle);
                setMoneyCell(row, 8, nz(r.remainingAmount()), moneyStyle);
                setCell(row, 9, safe(r.paymentMethod()), rowStyle);
                setCell(row, 10, safe(r.status()), rowStyle);
                setCell(row, 11, safe(r.description()), rowStyle);

                sumPaid = sumPaid.add(nz(r.amount()));
                sumDiscount = sumDiscount.add(nz(r.discount()));
                sumRemaining = sumRemaining.add(nz(r.remainingAmount()));
            }

            // Totals row
            Row totalRow = sheet.createRow(rowIdx++);
            Cell totalLabel = totalRow.createCell(0);
            mergeRowCells(totalRow, 0, 4);
            totalLabel.setCellValue("TOTAL (" + rows.size() + " paiement" + (rows.size() != 1 ? "s" : "") + ")");
            totalLabel.setCellStyle(totalStyle);
            for (int i = 1; i <= 4; i++) {
                totalRow.getCell(i).setCellStyle(totalStyle);
            }
            setCell(totalRow, 5, "", totalStyle);
            setMoneyCell(totalRow, 6, sumPaid, totalStyle);
            setMoneyCell(totalRow, 7, sumDiscount, totalStyle);
            setMoneyCell(totalRow, 8, sumRemaining, totalStyle);
            for (int i = 9; i <= 11; i++) {
                setCell(totalRow, i, "", totalStyle);
            }

            // Column widths
            int[] widths = {6, 22, 28, 18, 22, 12, 16, 16, 16, 14, 12, 32};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            // Freeze and auto-filter
            sheet.createFreezePane(0, firstDataRow);
            if (firstDataRow < rowIdx) {
                sheet.setAutoFilter(new CellRangeAddress(firstDataRow - 1, firstDataRow - 1, 0, 11));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.dispose();
            return out.toByteArray();
        }
    }

    private void mergeRow(Sheet sheet, int rowIdx, int lastCol) {
        Row row = sheet.createRow(rowIdx);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
        // The merged region requires all cells to exist for consistent styling
        for (int i = 1; i <= lastCol; i++) {
            row.createCell(i);
        }
    }

    private void mergeRowCells(Row row, int firstCol, int lastCol) {
        // row is already in a merged region from mergeRow; just ensure cells exist
        for (int i = firstCol; i <= lastCol; i++) {
            if (row.getCell(i) == null) {
                row.createCell(i);
            }
        }
    }

    private CellStyle buildTitleStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle buildSubtitleStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle buildMetaStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle buildExcelHeaderStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle buildBodyStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle buildAltBodyStyle(SXSSFWorkbook wb) {
        CellStyle style = buildBodyStyle(wb);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle buildMoneyStyle(SXSSFWorkbook wb) {
        CellStyle style = buildBodyStyle(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.##"));
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle buildDateStyle(SXSSFWorkbook wb) {
        CellStyle style = buildBodyStyle(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("dd/mm/yyyy"));
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle buildTotalStyle(SXSSFWorkbook wb) {
        CellStyle style = buildMoneyStyle(wb);
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.MEDIUM);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.MEDIUM);
        return style;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setMoneyCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private void setDateCell(Row row, int col, LocalDate date, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        cell.setCellStyle(style);
    }

    // ---------------------------------------------------------------------------
    // PDF (iText7)
    // ---------------------------------------------------------------------------

    private byte[] buildPdf(ExportContext ctx) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.getDocumentInfo().setTitle("Liste des paiements - " + safe(ctx.year().getLibelleAcademicYear()));
            pdf.getDocumentInfo().setAuthor(safe(ctx.generatedBy()));
            pdf.getDocumentInfo().setCreator("EcoleDesk Export Engine");

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler(ctx));

            PageSize pageSize = PageSize.A4.rotate();
            pdf.setDefaultPageSize(pageSize);

            Document doc = new Document(pdf, pageSize);
            doc.setMargins(18, 18, 36, 18);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Title block
            doc.add(new Paragraph(safe(ctx.schoolName()))
                    .setFont(bold)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2)
                    .setFontColor(ACCENT));
            doc.add(new Paragraph("LISTE DES PAIEMENTS")
                    .setFont(bold)
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2)
                    .setFontColor(ACCENT));
            doc.add(new Paragraph("Annee scolaire " + safe(ctx.year().getLibelleAcademicYear()))
                    .setFont(regular)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4)
                    .setFontColor(ColorConstants.DARK_GRAY));
            doc.add(new Paragraph("Genere le " + ctx.generatedAt().format(DD_MM_YYYY) + " a " + ctx.generatedAt().format(HH_MM) + " par " + safe(ctx.generatedBy()))
                    .setFont(regular)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(2)
                    .setFontColor(ColorConstants.GRAY));
            SolidLine solidLine = new SolidLine(0.5f);
            solidLine.setColor(ColorConstants.LIGHT_GRAY);
            doc.add(new LineSeparator(solidLine)
                    .setMarginBottom(6));;

            // Filters
            doc.add(new Paragraph("Filtres: " + safe(ctx.filtersDescription()))
                    .setFont(regular)
                    .setFontSize(8)
                    .setMarginBottom(10)
                    .setFontColor(ColorConstants.GRAY));

            List<PaymentExportRow> rows = ctx.rows();

            if (rows.isEmpty()) {
                doc.add(new Paragraph("Aucun paiement a exporter pour les criteres selectionnes.")
                        .setFont(regular)
                        .setFontSize(11)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(40));
            } else {
                // Table: 10 columns (skip description to avoid overflow in PDF landscape)
                Table table = new Table(UnitValue.createPercentArray(
                        new float[]{7, 15, 20, 14, 18, 12, 12, 12, 13, 10}))
                        .useAllAvailableWidth();

                String[] headers = {"Recu", "Eleve", "Classe", "Type", "Date", "Montant", "Remise", "Restant", "Statut", "Methode"};
                for (String h : headers) {
                    table.addHeaderCell(pdfHeaderCell(h, bold));
                }

                BigDecimal sumPaid = BigDecimal.ZERO;
                BigDecimal sumDiscount = BigDecimal.ZERO;
                BigDecimal sumRemaining = BigDecimal.ZERO;

                for (PaymentExportRow r : rows) {
                    boolean alt = rows.indexOf(r) % 2 != 0;
                    DeviceRgb bg = alt ? ALT_ROW : null;

                    table.addCell(pdfCell(safe(r.receiptNumber()), bg));
                    table.addCell(pdfCell(safe(r.studentName()), bg));
                    table.addCell(pdfCell(safe(r.className()), bg));
                    table.addCell(pdfCell(safe(r.type()), bg));
                    table.addCell(pdfCell(r.paymentDate() != null ? r.paymentDate().format(DD_MM_YYYY) : "-", bg));
                    table.addCell(pdfMoneyCell(nz(r.amount()), bg));
                    table.addCell(pdfMoneyCell(nz(r.discount()), bg));
                    table.addCell(pdfMoneyCell(nz(r.remainingAmount()), bg));
                    table.addCell(pdfStatusCell(safe(r.status()), bg));
                    table.addCell(pdfCell(safe(r.paymentMethod()), bg));

                    sumPaid = sumPaid.add(nz(r.amount()));
                    sumDiscount = sumDiscount.add(nz(r.discount()));
                    sumRemaining = sumRemaining.add(nz(r.remainingAmount()));
                }

                // Totals row
                DeviceRgb totalBg = TOTAL_BG;
                table.addCell(pdfCell("TOTAL", totalBg).setBold());
                table.addCell(pdfCell(rows.size() + " paiement" + (rows.size() != 1 ? "s" : ""), totalBg));
                table.addCell(pdfCell("", totalBg));
                table.addCell(pdfCell("", totalBg));
                table.addCell(pdfCell("", totalBg));
                table.addCell(pdfMoneyCell(sumPaid, totalBg).setBold());
                table.addCell(pdfMoneyCell(sumDiscount, totalBg).setBold());
                table.addCell(pdfMoneyCell(sumRemaining, totalBg).setBold());
                table.addCell(pdfCell("", totalBg));
                table.addCell(pdfCell("", totalBg));

                doc.add(table);
            }

            doc.close();
            return out.toByteArray();
        }
    }

    private com.itextpdf.layout.element.Cell pdfHeaderCell(String text, PdfFont bold) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(8))
                .setBackgroundColor(HEADER_BG)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(4)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f));
    }

    private com.itextpdf.layout.element.Cell pdfCell(String text, DeviceRgb bg) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text != null ? text : "-").setFontSize(8))
                .setPadding(3)
                .setTextAlignment(TextAlignment.LEFT);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        cell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.25f));
        return cell;
    }

    private com.itextpdf.layout.element.Cell pdfMoneyCell(BigDecimal value, DeviceRgb bg) {
        DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.FRANCE));
        String text = df.format(value) + " XOF";
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setFontSize(8))
                .setPadding(3)
                .setTextAlignment(TextAlignment.RIGHT);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        cell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.25f));
        return cell;
    }

    private com.itextpdf.layout.element.Cell pdfStatusCell(String status, DeviceRgb bg) {
        DeviceRgb color = switch (safe(status).toUpperCase(Locale.ROOT)) {
            case "PAID" -> new DeviceRgb(34, 197, 94);
            case "LATE" -> new DeviceRgb(239, 68, 68);
            case "CANCELLED" -> new DeviceRgb(156, 163, 175);
            default -> new DeviceRgb(245, 158, 11);
        };
        com.itextpdf.layout.element.Cell cell = pdfCell(status, bg);
        cell.setFontColor(color);
        return cell;
    }

    // ---------------------------------------------------------------------------
    // Page footer event
    // ---------------------------------------------------------------------------

    private static class FooterEventHandler implements IEventHandler {
        private final ExportContext ctx;

        FooterEventHandler(ExportContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdf.getPageNumber(page);

            Rectangle pageSize = page.getPageSize();
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdf);
            canvas.beginText();
            try {
                canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 8);
                String footer = "Page " + pageNumber
                        + "  |  " + safe(ctx.schoolName()) + "  |  Genere le " + ctx.generatedAt().format(DD_MM_YYYY);
                float x = pageSize.getLeft() + 18;
                float y = pageSize.getBottom() + 18;
                canvas.moveText(x, y);
                canvas.showText(footer);
                canvas.endText();
            } catch (Exception ex) {
                canvas.endText();
            }
            canvas.release();
        }
    }

    // ---------------------------------------------------------------------------
    // Utility
    // ---------------------------------------------------------------------------

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String fullName(com.school.platform.enrollment.domain.model.Student student) {
        if (student == null) {
            return "";
        }
        return (safe(student.getLastNameStudent()) + " " + safe(student.getFirstNameStudent())).trim();
    }

    private static String paymentStatus(com.school.platform.billing.domain.model.Paiement p) {
        if (p.getCancelledAt() != null) {
            return "CANCELLED";
        }
        if (p.getMontantRestant() == null || p.getMontantRestant().compareTo(BigDecimal.ZERO) == 0) {
            return "PAID";
        }
        if (p.getDueDate() != null && p.getDueDate().isBefore(LocalDate.now())) {
            return "LATE";
        }
        return "PENDING";
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "export";
        }
        String cleaned = name.replaceAll("[^a-zA-Z0-9\\-_ ]", "").trim();
        if (cleaned.isBlank()) {
            return "export";
        }
        return cleaned.replaceAll("\\s+", "-").toLowerCase(Locale.ROOT);
    }
}
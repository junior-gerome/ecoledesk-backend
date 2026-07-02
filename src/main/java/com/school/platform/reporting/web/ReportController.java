package com.school.platform.reporting.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.reporting.application.ReportService;
import com.school.platform.attendance.domain.model.Absence;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.enrollment.domain.model.InscriptionStudent;
import com.school.platform.billing.domain.model.Paiement;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Rapports", description = "API de generation des rapports")
public class ReportController {

    private final ReportService reportService;
    private final InscriptionStudentRepository inscriptionRepository;
    private final GradeRepository gradeRepository;
    private final AbsenceRepository absenceRepository;
    private final PaiementRepository paiementRepository;

    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        List<InscriptionStudent> inscriptions = classId == null
                ? inscriptionRepository.findAll()
                : inscriptionRepository.findByClasseRoomId(classId);

        List<Map<String, Object>> rows = inscriptions.stream().map(inscription -> {
            Long studentId = inscription.getStudent().getId();
            List<Grade> grades = gradeRepository.findByStudentId(studentId);
            double average = grades.stream().mapToDouble(grade -> grade.getGrade().doubleValue()).average().orElse(0);
            long absences = absenceRepository.findByStudentIdInAndDateBetween(List.of(studentId), start, end).stream()
                    .filter(a -> "ABSENT".equals(a.getStatus()))
                    .count();
            double attendanceRate = Math.max(0, 100 - absences * 5);
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", studentId);
            row.put("studentName", inscription.getStudent().getFirstNameStudent() + " " + inscription.getStudent().getLastNameStudent());
            row.put("className", inscription.getClasseRoom().getNameClasse());
            row.put("averageGrade", average);
            row.put("attendanceRate", attendanceRate);
            row.put("rank", 0);
            return row;
        }).collect(Collectors.toList());

        List<Map<String, Object>> ranked = java.util.stream.IntStream.range(0, rows.size())
                .mapToObj(index -> {
                    Map<String, Object> row = new HashMap<>(rows.stream()
                            .sorted(Comparator.comparingDouble((Map<String, Object> r) -> -((Number) r.get("averageGrade")).doubleValue()))
                            .collect(Collectors.toList()).get(index));
                    row.put("rank", index + 1);
                    return row;
                })
                .collect(Collectors.toList());

        double avg = ranked.stream().mapToDouble(row -> ((Number) row.get("averageGrade")).doubleValue()).average().orElse(0);
        double attendance = ranked.stream().mapToDouble(row -> ((Number) row.get("attendanceRate")).doubleValue()).average().orElse(100);
        long atRisk = ranked.stream().filter(row -> ((Number) row.get("averageGrade")).doubleValue() < 10 || ((Number) row.get("attendanceRate")).doubleValue() < 80).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", ranked.size());
        summary.put("averageGrade", avg);
        summary.put("attendanceRate", attendance);
        summary.put("atRiskCount", atRisk);

        Map<String, Object> response = new HashMap<>();
        response.put("generatedAt", LocalDateTime.now().toString());
        response.put("periodLabel", start + " - " + end);
        response.put("summary", summary);
        response.put("topStudents", ranked.stream().limit(10).collect(Collectors.toList()));
        response.put("riskStudents", ranked.stream()
                .filter(row -> ((Number) row.get("averageGrade")).doubleValue() < 10 || ((Number) row.get("attendanceRate")).doubleValue() < 80)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/financial")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Map<String, Object>> getFinancialReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().withDayOfYear(1);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(start, end);

        BigDecimal totalRevenue = paiements.stream()
                .map(p -> amountOrZero(p.getMontantPaye()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = paiements.stream()
                .map(p -> amountOrZero(p.getMontantRestant()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long paidCount = paiements.stream()
                .filter(p -> p.getMontantRestant() == null || p.getMontantRestant().compareTo(BigDecimal.ZERO) == 0)
                .count();

        Map<String, BigDecimal> byMonth = paiements.stream().collect(Collectors.groupingBy(
                p -> YearMonth.from(p.getDatePaiement()).toString(),
                Collectors.reducing(BigDecimal.ZERO, p -> amountOrZero(p.getMontantPaye()), BigDecimal::add)));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalOutstanding", totalOutstanding);
        summary.put("paidCount", paidCount);
        summary.put("unpaidCount", Math.max(0, paiements.size() - paidCount));

        Map<String, Object> response = new HashMap<>();
        response.put("generatedAt", LocalDateTime.now().toString());
        response.put("periodLabel", start + " - " + end);
        response.put("summary", summary);
        response.put("revenueByMonth", byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> Map.of("month", entry.getKey(), "amount", entry.getValue()))
                .collect(Collectors.toList()));
        response.put("topOutstanding", paiements.stream()
                .filter(p -> p.getMontantRestant() != null && p.getMontantRestant().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(Paiement::getMontantRestant).reversed())
                .limit(10)
                .map(p -> Map.of(
                        "studentName", p.getStudent().getFirstNameStudent() + " " + p.getStudent().getLastNameStudent(),
                        "className", p.getInscriptionStudent().getClasseRoom().getNameClasse(),
                        "amount", p.getMontantRestant()))
                .collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generer le bulletin de notes", description = "Genere le bulletin de notes d'un eleve au format PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF genere avec succes"),
            @ApiResponse(responseCode = "404", description = "Eleve non trouve")
    })
    @GetMapping("/bulletin/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> generateStudentReport(
            @Parameter(name = "studentId", description = "ID de l'eleve", in = ParameterIn.PATH) @PathVariable Long studentId,
            @Parameter(name = "period", description = "Periode (optionnelle)", in = ParameterIn.QUERY) @RequestParam(required = false) String period) {
        byte[] pdf = reportService.generateStudentReport(studentId, period);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bulletin.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Generer le rapport de performance de classe", description = "Genere un rapport de performance d'une classe pour une periode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF genere avec succes"),
            @ApiResponse(responseCode = "404", description = "Classe non trouvee")
    })
    @GetMapping("/classe/{classId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> generateClassReport(
            @Parameter(name = "classId", description = "ID de la classe", in = ParameterIn.PATH) @PathVariable Long classId,
            @Parameter(name = "period", description = "Periode (ex: TRIMESTRE1)", in = ParameterIn.QUERY) @RequestParam(required = false) String period) {
        byte[] pdf = reportService.generateClassReport(classId, period);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-classe.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/classe/{classId}/bulletins.zip")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> generateClassBulletinsZip(
            @PathVariable Long classId,
            @RequestParam(required = false) String period) {
        List<InscriptionStudent> inscriptions = inscriptionRepository.findByClasseRoomId(classId);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output)) {
                for (InscriptionStudent inscription : inscriptions) {
                    if (inscription.getStudent() == null || inscription.getStudent().getId() == null) {
                        continue;
                    }

                    byte[] pdf = reportService.generateStudentReport(inscription.getStudent().getId(), period);
                    String studentName = inscription.getStudent().getLastNameStudent() + "-"
                            + inscription.getStudent().getFirstNameStudent();
                    ZipEntry entry = new ZipEntry(sanitizeFileName("bulletin-" + studentName + ".pdf"));
                    zip.putNextEntry(entry);
                    zip.write(pdf);
                    zip.closeEntry();
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bulletins-classe-" + classId + ".zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(output.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la generation du ZIP des bulletins", e);
        }
    }

    @Operation(summary = "Generer le rapport des paiements", description = "Genere un recapitulatif des paiements sur une periode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF genere avec succes")
    })
    @GetMapping("/paiements")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<byte[]> generatePaymentReport(
            @Parameter(name = "startDate", description = "Date de debut", in = ParameterIn.QUERY) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(name = "endDate", description = "Date de fin", in = ParameterIn.QUERY) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        byte[] pdf = reportService.generatePaymentReport(startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-paiements.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String sanitizeFileName(String value) {
        return value == null || value.isBlank()
                ? "bulletin.pdf"
                : value.replaceAll("[^a-zA-Z0-9._-]", "-");
    }

    private BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Operation(summary = "Generer le rapport de progression d'un eleve", description = "Genere un rapport detaille de progression pour un eleve")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF genere avec succes"),
            @ApiResponse(responseCode = "404", description = "Eleve non trouve")
    })
    @GetMapping("/performance/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> generateStudentPerformanceReport(
            @Parameter(name = "studentId", description = "ID de l'eleve", in = ParameterIn.PATH) @PathVariable Long studentId) {
        byte[] pdf = reportService.generateStudentProgressReport(studentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-performance.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

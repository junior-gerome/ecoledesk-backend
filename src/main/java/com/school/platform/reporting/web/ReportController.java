package com.school.platform.reporting.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.reporting.application.ReportService;
import com.school.platform.attendance.domain.model.Absence;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.billing.domain.model.Paiement;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
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
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AbsenceRepository absenceRepository;
    private final PaiementRepository paiementRepository;

    @Transactional(readOnly = true)
    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = Math.min(Math.max(size, 1), 100);

        List<Enrollment> enrollments = classId == null
                ? enrollmentRepository.findByStatusFetchStudentAndClassroom(EnrollmentStatus.CONFIRMED)
                : enrollmentRepository.findByClassroomIdAndStatusFetchStudentAndClassroom(classId, EnrollmentStatus.CONFIRMED);

        List<Long> studentIds = enrollments.stream()
                .map(enrollment -> enrollment.getStudent() != null ? enrollment.getStudent().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Double> averagesByStudent = new HashMap<>();
        Map<Long, Long> absencesByStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            for (Object[] row : gradeRepository.findAverageGradeByStudentIds(studentIds)) {
                averagesByStudent.put(((Number) row[0]).longValue(), ((Number) row[1]).doubleValue());
            }
            for (Absence absence : absenceRepository.findByStudentIdInAndDateBetween(studentIds, start, end)) {
                if ("ABSENT".equals(absence.getStatus())
                        && absence.getStudent() != null
                        && absence.getStudent().getId() != null) {
                    absencesByStudent.merge(absence.getStudent().getId(), 1L, Long::sum);
                }
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>(enrollments.size());
        double sumGrades = 0;
        double sumAttendance = 0;
        long atRiskCount = 0;
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            if (student == null) {
                continue;
            }
            Long studentId = student.getId();
            double average = averagesByStudent.getOrDefault(studentId, 0.0);
            long absences = absencesByStudent.getOrDefault(studentId, 0L);
            double attendanceRate = Math.max(0, 100 - absences * 5);
            sumGrades += average;
            sumAttendance += attendanceRate;
            if (average < 10 || attendanceRate < 80) {
                atRiskCount++;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", studentId);
            row.put("studentName", student.getFirstNameStudent() + " " + student.getLastNameStudent());
            row.put("className", enrollment.getClassroom() != null ? enrollment.getClassroom().getNameClasse() : "");
            row.put("averageGrade", average);
            row.put("attendanceRate", attendanceRate);
            row.put("rank", 0);
            rows.add(row);
        }

        rows.sort(Comparator.comparingDouble(
                (Map<String, Object> r) -> ((Number) r.get("averageGrade")).doubleValue()).reversed());
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).put("rank", i + 1);
        }

        int total = rows.size();
        double avg = total > 0 ? sumGrades / total : 0;
        double attendance = total > 0 ? sumAttendance / total : 100;

        List<Map<String, Object>> riskStudents = rows.stream()
                .filter(row -> ((Number) row.get("averageGrade")).doubleValue() < 10
                        || ((Number) row.get("attendanceRate")).doubleValue() < 80)
                .collect(Collectors.toList());
        long riskTotal = riskStudents.size();
        int from = Math.min(resolvedPage * resolvedSize, riskStudents.size());
        int to = Math.min(from + resolvedSize, riskStudents.size());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", total);
        summary.put("averageGrade", avg);
        summary.put("attendanceRate", attendance);
        summary.put("atRiskCount", atRiskCount);

        Map<String, Object> response = new HashMap<>();
        response.put("generatedAt", LocalDateTime.now().toString());
        response.put("periodLabel", start + " - " + end);
        response.put("summary", summary);
        response.put("topStudents", rows.stream().limit(10).collect(Collectors.toList()));
        response.put("riskStudents", riskStudents.subList(from, to));
        response.put("riskTotalElements", riskTotal);
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
                        "className", p.getEnrollment() != null && p.getEnrollment().getClassroom() != null
                                ? p.getEnrollment().getClassroom().getNameClasse() : "",
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
            @Parameter(name = "period", description = "Periode (optionnelle)", in = ParameterIn.QUERY) @RequestParam(required = false) String period,
            @Parameter(name = "untilPeriod", description = "Borne cumulative (optionnelle, ex: Sequence 2)", in = ParameterIn.QUERY) @RequestParam(required = false) String untilPeriod) {
        byte[] pdf = reportService.generateStudentReport(studentId, period, untilPeriod);
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
            @RequestParam(required = false) String period,
            @RequestParam(required = false) List<Long> studentIds,
            @RequestParam(required = false) String untilPeriod) {
        List<Enrollment> enrollments = enrollmentRepository.findByClassroomIdAndStatus(classId, EnrollmentStatus.CONFIRMED);

        if (studentIds != null && !studentIds.isEmpty()) {
            enrollments = enrollments.stream()
                    .filter(enrollment -> enrollment.getStudent() != null
                            && enrollment.getStudent().getId() != null
                            && studentIds.contains(enrollment.getStudent().getId()))
                    .collect(Collectors.toList());
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output)) {
                for (Enrollment enrollment : enrollments) {
                    if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null) {
                        continue;
                    }

                    byte[] pdf = reportService.generateStudentReport(enrollment.getStudent().getId(), period, untilPeriod);
                    String studentName = enrollment.getStudent().getLastNameStudent() + "-"
                            + enrollment.getStudent().getFirstNameStudent();
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

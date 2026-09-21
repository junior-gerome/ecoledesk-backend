package com.school.platform.billing.adapter.in.rest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.billing.application.dto.PaymentDTO;
import com.school.platform.billing.application.dto.PaymentExportRequest;
import com.school.platform.billing.application.dto.PaymentRequest;
import com.school.platform.billing.application.PaymentExportService;
import com.school.platform.billing.application.PaymentService;
import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.reporting.application.PdfGenerationService;
import com.school.platform.shared.web.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentExportService paymentExportService;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping
    public ResponseEntity<PageResponse<Map<String, Object>>> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) TypePaiement type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String receiptNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "datePaiement,desc") String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), paymentSort(sort));
        Page<Map<String, Object>> result = paymentService.listPaymentsPage(
                studentId, status, type, startDate, endDate, q, receiptNumber, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    private static Sort paymentSort(String sort) {
        String[] parts = sort.split(",");
        String property = parts[0];
        String mapped = switch (property) {
            case "id", "datePaiement", "receiptNumber", "montantPaye" -> property;
            default -> "datePaiement";
        };
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, mapped);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> summary() {
        return ResponseEntity.ok(paymentService.summarizePayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentResponse(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        paymentService.cancelPayment(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestBody(required = false) PaymentExportRequest request,
            Authentication authentication) {
        PaymentExportRequest req = request != null ? request : PaymentExportRequest.empty();
        String user = authentication != null ? authentication.getName() : "system";
        PaymentExportService.ExportContext ctx = paymentExportService.resolveExportContext(req, user);
        byte[] content = paymentExportService.generateExcel(ctx);
        String filename = paymentExportService.exportFileName(ctx, "xlsx");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + rfc5987(filename))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestBody(required = false) PaymentExportRequest request,
            Authentication authentication) {
        PaymentExportRequest req = request != null ? request : PaymentExportRequest.empty();
        String user = authentication != null ? authentication.getName() : "system";
        PaymentExportService.ExportContext ctx = paymentExportService.resolveExportContext(req, user);
        byte[] content = paymentExportService.generatePdf(ctx);
        String filename = paymentExportService.exportFileName(ctx, "pdf");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + rfc5987(filename))
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    private static String rfc5987(String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return "filename*=UTF-8''" + encoded;
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long id) throws Exception {
        PaymentDTO payment = paymentService.getPaymentReceiptDto(id);
        byte[] pdf = pdfGenerationService.generatePaymentReceipt(payment);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu-paiement-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
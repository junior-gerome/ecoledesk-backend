package com.school.billing.adapter.in.rest;

import com.school.billing.application.port.in.PaymentFilter;
import com.school.billing.application.port.in.PaymentSummary;
import com.school.billing.application.port.in.PaymentUseCase;
import com.school.billing.domain.model.PaymentStatus;
import com.school.billing.domain.model.PaymentType;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentUseCase useCase;

    public PaymentController(PaymentUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<PaymentResponse> list(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return useCase.list(new PaymentFilter(studentId, status, type, startDate, endDate)).stream()
                .map(PaymentRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return PaymentRestMapper.toResponse(useCase.get(id));
    }

    @PostMapping
    public PaymentResponse create(@Valid @RequestBody PaymentRequest request) {
        return PaymentRestMapper.toResponse(useCase.create(PaymentRestMapper.toDomain(request)));
    }

    @PutMapping("/{id}")
    public PaymentResponse update(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return PaymentRestMapper.toResponse(useCase.update(id, PaymentRestMapper.toDomain(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public PaymentSummary summary() {
        return useCase.summarize();
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(useCase.generateReceipt(id));
    }
}

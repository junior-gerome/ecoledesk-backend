package com.school.platform.enrollment.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.enrollment.application.dto.InscriptionStudentDTO;
import com.school.platform.enrollment.application.dto.PreinscriptionDecisionRequest;
import com.school.platform.enrollment.application.dto.PreinscriptionStatusResponse;
import com.school.platform.enrollment.application.InscriptionStudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/preinscription")
@RequiredArgsConstructor
public class PreinscriptionController {
    private final InscriptionStudentService inscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<InscriptionStudentDTO> createPreinscription(
            @Valid @RequestBody InscriptionStudentDTO request) {
        return ResponseEntity.ok(inscriptionService.createPreinscription(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<InscriptionStudentDTO>> getAll() {
        return ResponseEntity.ok(inscriptionService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<InscriptionStudentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<InscriptionStudentDTO> updatePreinscription(
            @PathVariable Long id,
            @RequestBody InscriptionStudentDTO request) {
        return ResponseEntity.ok(inscriptionService.updateInscription(id, request));
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<PreinscriptionStatusResponse> validatePreinscription(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.validatePreinscription(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<PreinscriptionStatusResponse> rejectPreinscription(
            @PathVariable Long id,
            @Valid @RequestBody PreinscriptionDecisionRequest request) {
        return ResponseEntity.ok(inscriptionService.rejectPreinscription(id, request.getJustification()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<PreinscriptionStatusResponse> cancelPreinscription(
            @PathVariable Long id,
            @Valid @RequestBody PreinscriptionDecisionRequest request) {
        return ResponseEntity.ok(inscriptionService.cancelPreinscription(id, request.getJustification()));
    }
}

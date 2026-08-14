package com.school.platform.academic.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.school.platform.academic.application.interfaces.AcademicYearService;
import com.school.platform.academic.application.dto.AcademicYearDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/academic-year")
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<AcademicYearDTO> createAcademicYear(@RequestBody AcademicYearDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYearService.createAcademicYear(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AcademicYearDTO>> getAllAcademicYear() {
        return ResponseEntity.ok(academicYearService.getAllAcademicYear());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<AcademicYearDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.getById(id));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<AcademicYearDTO> getActiveAcademicYear() {
        try {
            return ResponseEntity.ok(academicYearService.getActiveAcademicYear());
        } catch (com.school.platform.shared.domain.exception.ResourceNotFoundException ex) {
            // No active academic year configured yet — return 200 with empty body
            // so the frontend can handle it gracefully without a 404 error.
            return ResponseEntity.ok().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<AcademicYearDTO> updateAcademicYear(@PathVariable Long id, @RequestBody AcademicYearDTO dto) {
        return ResponseEntity.ok(academicYearService.updateAcademicYear(id, dto));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<AcademicYearDTO> activateAcademicYear(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.activateAcademicYear(id));
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<AcademicYearDTO> inactivateAcademicYear(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.inactivateAcademicYear(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.noContent().build();
    }
}

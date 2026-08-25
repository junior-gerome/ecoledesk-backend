package com.school.platform.enrollment.web;

import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentMediumDTO;
import com.school.platform.enrollment.application.interfaces.PreEnrollmentQueryService;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.shared.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de consultation (lecture seule) pour les pré-inscriptions.
 * Les commandes (création, workflow) restent dans PreEnrollmentController.
 */
@RestController
@RequestMapping("/pre-enrollments")
@RequiredArgsConstructor
public class PreEnrollmentQueryController {

    private final PreEnrollmentQueryService queryService;

    /**
     * GET /pre-enrollments
     * Liste paginée de toutes les pré-inscriptions (vue medium).
     * Paramètres: page (default 0), size (default 20), sort (default creationDate,desc)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<PageResponse<PreEnrollmentMediumDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "creationDate,desc") String sort) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
        Page<PreEnrollmentMediumDTO> result = queryService.getAll(pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    /**
     * GET /pre-enrollments/{id}
     * Détail complet d'une pré-inscription (candidat, tuteurs, documents).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<PreEnrollmentFullDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getById(id));
    }

    /**
     * GET /pre-enrollments/by-status/{status}
     * Liste des pré-inscriptions filtrées par statut (DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED…).
     */
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<PreEnrollmentBasicDTO>> getByStatus(
            @PathVariable PreEnrollmentStatus status) {
        return ResponseEntity.ok(queryService.getByStatus(status));
    }

    /**
     * GET /pre-enrollments/by-academic-year/{academicYearId}
     * Liste des pré-inscriptions pour une année scolaire donnée.
     */
    @GetMapping("/by-academic-year/{academicYearId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<PreEnrollmentBasicDTO>> getByAcademicYear(
            @PathVariable Long academicYearId) {
        return ResponseEntity.ok(queryService.getByAcademicYear(academicYearId));
    }
}

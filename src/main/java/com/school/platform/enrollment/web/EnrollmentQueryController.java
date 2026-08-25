package com.school.platform.enrollment.web;

import com.school.platform.enrollment.application.dto.enrollment.EnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentMediumDTO;
import com.school.platform.enrollment.application.interfaces.EnrollmentQueryService;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
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
 * Endpoints de consultation (lecture seule) pour les inscriptions.
 * Les commandes (confirm, cancel, withdraw) restent dans EnrollmentController.
 */
@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentQueryController {

    private final EnrollmentQueryService queryService;

    /**
     * GET /enrollments
     * Liste paginée de toutes les inscriptions (vue medium avec données étudiant et classe).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<PageResponse<EnrollmentMediumDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "enrollmentDate,desc") String sort) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
        Page<EnrollmentMediumDTO> result = queryService.getAll(pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    /**
     * GET /enrollments/{id}
     * Détail complet d'une inscription (étudiant complet, classe, année scolaire).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<EnrollmentFullDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getById(id));
    }

    /**
     * GET /enrollments/by-class/{classroomId}
     * Liste des inscriptions d'une classe avec filtre optionnel sur le statut.
     * Utilisé notamment pour la saisie de notes et les documents scolaires.
     * Par défaut retourne les inscriptions CONFIRMED.
     */
    @GetMapping("/by-class/{classroomId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<EnrollmentBasicDTO>> getByClassroom(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "CONFIRMED") EnrollmentStatus status) {
        return ResponseEntity.ok(queryService.getByClassroom(classroomId, status));
    }

    /**
     * GET /enrollments/by-status/{status}
     * Liste des inscriptions filtrées par statut.
     */
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<EnrollmentBasicDTO>> getByStatus(
            @PathVariable EnrollmentStatus status) {
        return ResponseEntity.ok(queryService.getByStatus(status));
    }

    /**
     * GET /enrollments/count/{status}
     * Nombre d'inscriptions par statut — utile pour les KPIs dashboard.
     */
    @GetMapping("/count/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Long> countByStatus(@PathVariable EnrollmentStatus status) {
        return ResponseEntity.ok(queryService.countByStatus(status));
    }
}

package com.school.platform.academic.web;

import java.util.List;

import com.school.platform.academic.application.AffectationService;
import com.school.platform.academic.application.dto.AffectationCreateRequest;
import com.school.platform.academic.application.dto.AffectationResponse;
import com.school.platform.academic.application.dto.AffectationUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/affectations")
@RequiredArgsConstructor
public class AffectationController {

    private final AffectationService affectationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<AffectationResponse> create(@Valid @RequestBody AffectationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(affectationService.createAffectation(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AffectationResponse>> findAll() {
        return ResponseEntity.ok(affectationService.getAllAffectationResponses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<AffectationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(affectationService.getAffectationById(id));
    }

    @GetMapping("/by-class/{classId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AffectationResponse>> findByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(affectationService.getAffectationResponsesByClasse(classId));
    }

    @GetMapping("/by-teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AffectationResponse>> findByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(affectationService.getAffectationResponsesByTeacher(teacherId));
    }

    @GetMapping("/by-school-year/{schoolYearId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AffectationResponse>> findBySchoolYear(@PathVariable Long schoolYearId) {
        return ResponseEntity.ok(affectationService.getAffectationResponsesBySchoolYear(schoolYearId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<AffectationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AffectationUpdateRequest request) {
        return ResponseEntity.ok(affectationService.updateAffectation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        affectationService.deleteAffectation(id);
        return ResponseEntity.noContent().build();
    }
}

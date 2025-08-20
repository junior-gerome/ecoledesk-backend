package com.school.management.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.management.dto.GradeDTO;
import com.school.management.dto.PageResponse;
import com.school.management.service.GradeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Notes", description = "API de gestion des notes")
public class GradeController {

    private final GradeService gradeService;

    @Operation(summary = "Récupérer les notes par classe", description = "Récupère toutes les notes d'une classe pour une période donnée avec pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notes récupérées avec succès"),
        @ApiResponse(responseCode = "400", description = "Paramètres invalides"),
        @ApiResponse(responseCode = "404", description = "Classe non trouvée")
    })
    @GetMapping("/class/{classId}")
    public ResponseEntity<PageResponse<GradeDTO>> getGradesByClass(
        @Parameter(name="classId", description = "ID de la classe", in = ParameterIn.PATH) @PathVariable String classId,
        @Parameter(name="period", description = "Période (ex: TRIMESTRE1)", in = ParameterIn.QUERY) @RequestParam String period,
        @Parameter(name="page", description = "Numéro de page (commence à 0)", in = ParameterIn.QUERY) @RequestParam(defaultValue = "0") int page,
        @Parameter(name="size", description = "Taille de la page", in = ParameterIn.QUERY) @RequestParam(defaultValue = "10") int size
    ) {
        Page<GradeDTO> gradePage = gradeService.findByClassIdAndPeriod(Long.parseLong(classId), period, PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(gradePage));
    }

    @Operation(summary = "Récupérer les notes d'un élève", description = "Récupère toutes les notes d'un élève spécifique avec pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notes récupérées avec succès"),
        @ApiResponse(responseCode = "400", description = "ID d'élève invalide"),
        @ApiResponse(responseCode = "404", description = "Élève non trouvé")
    })
    @GetMapping("/student/{studentId}")
    public ResponseEntity<PageResponse<GradeDTO>> getGradesByStudent(
        @Parameter(name="studentId", description = "ID de l'élève", in = ParameterIn.PATH) @PathVariable String studentId,
        @Parameter(name="page", description = "Numéro de page (commence à 0)", in= ParameterIn.QUERY) @RequestParam(defaultValue = "0") int page,
        @Parameter(name="size", description = "Taille de la page", in = ParameterIn.QUERY) @RequestParam(defaultValue = "10") int size
    ) {
        Page<GradeDTO> gradePage = gradeService.findByStudentId(Long.parseLong(studentId), PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(gradePage));
    }

    @Operation(summary = "Ajouter une nouvelle note", description = "Ajoute une nouvelle note pour un élève")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Note ajoutée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de note invalides")
    })
    @PostMapping
    public ResponseEntity<GradeDTO> addGrade(
        @Parameter(name="grade", description = "Détails de la note", in = ParameterIn.QUERY) @Valid @RequestBody GradeDTO grade
    ) {
        // TODO: Implémenter la logique d'ajout
        return ResponseEntity.ok(grade);
    }

    @Operation(summary = "Mettre à jour une note", description = "Met à jour une note existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Note mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données de note invalides"),
        @ApiResponse(responseCode = "404", description = "Note non trouvée")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(
        @Parameter(name="id", description = "ID de la note", in = ParameterIn.PATH) @PathVariable Long id,
        @Parameter(name="grade", description = "Nouvelles données de la note", in = ParameterIn.QUERY) @Valid @RequestBody GradeDTO grade
    ) {
        // TODO: Implémenter la logique de mise à jour
        return ResponseEntity.ok(grade);
    }

    @Operation(summary = "Supprimer une note", description = "Supprime une note existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Note supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Note non trouvée")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(
        @Parameter(name="id", description = "ID de la note à supprimer", in = ParameterIn.PATH) @PathVariable Long id
    ) {
        // TODO: Implémenter la logique de suppression
        return ResponseEntity.ok().build();
    }
}
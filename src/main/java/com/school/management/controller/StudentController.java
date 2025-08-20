package com.school.management.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.school.management.dto.StudentDTO;
import com.school.management.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Élèves", description = "API de gestion des élèves")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Récupérer tous les élèves", description = "Renvoie la liste de tous les élèves enregistrés")
    @ApiResponse(responseCode = "200", description = "Liste des élèves récupérée avec succès")
    @GetMapping
    public ResponseEntity<?> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @Operation(summary = "Récupérer un élève par son ID", description = "Renvoie les détails d'un élève spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Élève trouvé"),
        @ApiResponse(responseCode = "404", description = "Élève non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(
        @Parameter(name = "id", description = "ID de l'élève", in = ParameterIn.PATH)
        @PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @Operation(summary = "Créer un nouvel élève", description = "Crée un nouvel élève avec les informations fournies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Élève créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données d'entrée invalides")
    })
    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(
        @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.createStudent(studentDTO));
    }

    @Operation(summary = "Mettre à jour un élève", description = "Met à jour les informations d'un élève existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Élève mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Élève non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(
        @Parameter(name = "id", description = "ID de l'élève", in = ParameterIn.PATH)
        @PathVariable Long id,
        @Valid @RequestBody StudentDTO studentDTO) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDTO));
    }

    @Operation(summary = "Supprimer un élève", description = "Supprime un élève de la base de données")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Élève supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Élève non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
        @Parameter(name = "id", description = "ID de l'élève à supprimer", in = ParameterIn.PATH)
        @PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Récupérer les élèves par classe", description = "Renvoie la liste des élèves d'une classe spécifique avec pagination")
    @ApiResponse(responseCode = "200", description = "Liste des élèves récupérée avec succès")
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<Page<StudentDTO>> getStudentsByClasse(
        @Parameter(name = "classeId", description = "ID de la classe", in = ParameterIn.PATH)
        @PathVariable Long classeId,
        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByClasse(classeId, pageable));
    }

    @Operation(summary = "Récupérer les élèves par parent", description = "Renvoie la liste des élèves d'un parent spécifique avec pagination")
    @ApiResponse(responseCode = "200", description = "Liste des élèves récupérée avec succès")
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Page<StudentDTO>> getStudentsByParent(
        @Parameter(name = "parentId", description = "ID du parent", in = ParameterIn.PATH)
        @PathVariable Long parentId,
        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByParent(parentId, pageable));
    }

    @Operation(summary = "Récupérer les élèves par section", description = "Renvoie la liste des élèves d'une section spécifique avec pagination")
    @ApiResponse(responseCode = "200", description = "Liste des élèves récupérée avec succès")
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<Page<StudentDTO>> getStudentsBySection(
        @Parameter(name = "sectionId", description = "ID de la section", in = ParameterIn.PATH)
        @PathVariable Long sectionId,
        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsBySection(sectionId, pageable));
    }
}

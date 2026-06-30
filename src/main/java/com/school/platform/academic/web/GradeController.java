package com.school.platform.academic.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.school.platform.academic.application.dto.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.GradeResponseDTO;
import com.school.platform.shared.web.PageResponse;
import com.school.platform.academic.application.GradeCalculationService;
import com.school.platform.academic.application.GradeCommandService;
import com.school.platform.academic.application.GradeQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "API de gestion des notes")
public class GradeController {

    private final GradeCommandService gradeCommandService;
    private final GradeQueryService gradeQueryService;
    private final GradeCalculationService gradeCalculationService;

    @Operation(summary = "Recuperer les notes par classe", description = "Recupere toutes les notes d'une classe pour une periode donnee avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes recuperees avec succes"),
            @ApiResponse(responseCode = "400", description = "Parametres invalides"),
            @ApiResponse(responseCode = "404", description = "Classe non trouvee")
    })
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<PageResponse<GradeResponseDTO>> getGradesByClass(
            @Parameter(name = "classId", description = "ID de la classe", in = ParameterIn.PATH) @PathVariable Long classId,
            @Parameter(name = "period", description = "Periode (ex: TRIMESTRE1)", in = ParameterIn.QUERY) @RequestParam String period,
            @Parameter(name = "page", description = "Numero de page (commence a 0)", in = ParameterIn.QUERY) @RequestParam(defaultValue = "0") int page,
            @Parameter(name = "size", description = "Taille de la page", in = ParameterIn.QUERY) @RequestParam(defaultValue = "10") int size) {
        Page<GradeResponseDTO> gradePage = gradeQueryService.findByClassIdAndPeriod(classId, period, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(gradePage));
    }

    @Operation(summary = "Recuperer les notes d'un eleve", description = "Recupere toutes les notes d'un eleve specifique avec pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes recuperees avec succes"),
            @ApiResponse(responseCode = "400", description = "ID d'eleve invalide"),
            @ApiResponse(responseCode = "404", description = "Eleve non trouve")
    })
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<PageResponse<GradeResponseDTO>> getGradesByStudent(
            @Parameter(name = "studentId", description = "ID de l'eleve", in = ParameterIn.PATH) @PathVariable Long studentId,
            @Parameter(name = "page", description = "Numero de page (commence a 0)", in = ParameterIn.QUERY) @RequestParam(defaultValue = "0") int page,
            @Parameter(name = "size", description = "Taille de la page", in = ParameterIn.QUERY) @RequestParam(defaultValue = "10") int size) {
        Page<GradeResponseDTO> gradePage = gradeQueryService.findByStudentId(studentId, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.from(gradePage));
    }

    @GetMapping("/stats/student/{studentId}/subject/{subjectId}/period/{period}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> getStudentGradeStats(
            @PathVariable Long studentId,
            @PathVariable Long subjectId,
            @PathVariable String period) {
        return ResponseEntity.ok(gradeCalculationService.getStudentGradeStats(studentId, subjectId, period));
    }

    @GetMapping("/stats/class/{classId}/subject/{subjectId}/period/{period}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> getClassGradeStats(
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @PathVariable String period) {
        return ResponseEntity.ok(gradeCalculationService.getClassGradeStats(classId, subjectId, period));
    }

    @GetMapping("/report/student/{studentId}/period/{period}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> getStudentReport(
            @PathVariable Long studentId,
            @PathVariable String period) {
        return ResponseEntity.ok(gradeCalculationService.getStudentReport(studentId, period));
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<GradeResponseDTO> getGradeById(@PathVariable Long id) {
        return ResponseEntity.ok(gradeQueryService.getGradeById(id));
    }

    @Operation(summary = "Ajouter une nouvelle note", description = "Ajoute une nouvelle note pour un eleve")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note ajoutee avec succes"),
            @ApiResponse(responseCode = "400", description = "Donnees de note invalides")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT')")
    public ResponseEntity<GradeResponseDTO> addGrade(
            @Parameter(name = "grade", description = "Details de la note", in = ParameterIn.QUERY) @Valid @RequestBody GradeCreateRequestDTO grade) {
        return ResponseEntity.ok(gradeCommandService.saveGrade(grade));
    }

    @Operation(summary = "Mettre a jour une note", description = "Met a jour une note existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note mise a jour avec succes"),
            @ApiResponse(responseCode = "400", description = "Donnees de note invalides"),
            @ApiResponse(responseCode = "404", description = "Note non trouvee")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ENSEIGNANT')")
    public ResponseEntity<GradeResponseDTO> updateGrade(
            @Parameter(name = "id", description = "ID de la note", in = ParameterIn.PATH) @PathVariable Long id,
            @Parameter(name = "grade", description = "Nouvelles donnees de la note", in = ParameterIn.QUERY) @Valid @RequestBody GradeCreateRequestDTO grade) {
        return ResponseEntity.ok(gradeCommandService.updateGrade(id, grade));
    }

    @Operation(summary = "Supprimer une note", description = "Supprime une note existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note supprimee avec succes"),
            @ApiResponse(responseCode = "404", description = "Note non trouvee")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGrade(
            @Parameter(name = "id", description = "ID de la note a supprimer", in = ParameterIn.PATH) @PathVariable Long id) {
        gradeCommandService.deleteGrade(id);
        return ResponseEntity.ok().build();
    }
}

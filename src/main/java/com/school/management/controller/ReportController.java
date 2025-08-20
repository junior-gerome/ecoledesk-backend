// package com.school.management.controller;

// import java.time.LocalDate;

// import com.school.management.service.ReportService;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.enums.ParameterIn;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/reports")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:4200")
// @Tag(name = "Rapports", description = "API de génération des rapports")
// public class ReportController {

//     private final ReportService reportService;

//     @Operation(summary = "Générer le bulletin de notes", description = "Génère le bulletin de notes d'un élève au format PDF")
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
//         @ApiResponse(responseCode = "404", description = "Élève non trouvé")
//     })
//     @GetMapping("/bulletin/{studentId}")
//     public ResponseEntity<byte[]> generateStudentReport(
//         @Parameter(name = "studentId", description = "ID de l'élève", in = ParameterIn.PATH)
//         @PathVariable Long studentId  // Changé de String à Long
//     ) throws Exception {
//         byte[] pdf = reportService.generateStudentProgressReport(studentId);
//         return ResponseEntity.ok()
//             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bulletin.pdf")
//             .contentType(MediaType.APPLICATION_PDF)
//             .body(pdf);
//     }

//     @Operation(
//         summary = "Générer le rapport de performance de la classe",
//         description = "Génère un rapport détaillé des performances d'une classe pour une période donnée"
//     )
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
//         @ApiResponse(responseCode = "404", description = "Classe non trouvée")
//     })
//     @GetMapping("/classe/{classId}")
//     public ResponseEntity<byte[]> generateClassReport(
//         @Parameter(name = "classId", description = "ID de la classe", in = ParameterIn.PATH)
//         @PathVariable Long classId,  // Changé de String à Long

//         @Parameter(name = "period", description = "Période (ex: TRIMESTRE1)", in = ParameterIn.QUERY)
//         @RequestParam String period
//     ) throws Exception {
//         byte[] pdf = reportService.generateClassReport(classId, period);  // Ajout du paramètre period
//         return ResponseEntity.ok()
//             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-classe.pdf")
//             .contentType(MediaType.APPLICATION_PDF)
//             .body(pdf);
//     }

//     @Operation(
//         summary = "Générer le rapport des paiements",
//         description = "Génère un récapitulatif des paiements sur une période donnée"
//     )
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "PDF généré avec succès")
//     })
//     @GetMapping("/paiements")
//     public ResponseEntity<byte[]> generatePaymentReport(
//         @Parameter(name = "startDate", description = "Date de début", in = ParameterIn.QUERY)
//         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//         LocalDate startDate,

//         @Parameter(name = "endDate", description = "Date de fin", in = ParameterIn.QUERY)
//         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//         LocalDate endDate
//     ) throws Exception {
//         byte[] pdf = reportService.generatePaymentReport(startDate, endDate);  // Changé le nom de la méthode
//         return ResponseEntity.ok()
//             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-paiements.pdf")
//             .contentType(MediaType.APPLICATION_PDF)
//             .body(pdf);
//     }


//     @Operation(
//         summary = "Générer le rapport de performance d'un élève",
//         description = "Génère un rapport de performance détaillé pour un élève spécifique"
//     )
//     @ApiResponses(value = {
//         @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
//         @ApiResponse(responseCode = "404", description = "Élève non trouvé")
//     })
//     @GetMapping("/performance/{studentId}")
//     public ResponseEntity<byte[]> generateStudentPerformanceReport(
//         @Parameter(name = "studentId", description = "ID de l'élève", in = ParameterIn.PATH)
//         @PathVariable Long studentId  // Changé de String à Long
//     ) throws Exception {
//         byte[] pdf = reportService.generateStudentReport(studentId, null);  // Méthode attendue
//         return ResponseEntity.ok()
//             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-performance.pdf")
//             .contentType(MediaType.APPLICATION_PDF)
//             .body(pdf);
//     }
// }
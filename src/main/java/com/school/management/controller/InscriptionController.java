package com.school.management.controller;

import com.school.management.dto.InscriptionRequest;
import com.school.management.dto.InscriptionStudentDTO;
import com.school.management.mappers.InscriptionStudentMapper;
import com.school.management.service.InscriptionStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inscription")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionStudentService inscriptionService;

    // ✅ Créer une inscription
    @PostMapping
    public ResponseEntity<InscriptionStudentDTO> createInscription(@RequestBody InscriptionRequest request) {
        InscriptionStudentDTO inscription = inscriptionService.createInscription(
                request.getStudent(),
                request.getClasseRoomId(),
                request.getMontantId(),
                request.getAnneeScolaireId()
        );
        return ResponseEntity.ok(inscription);
    }

    // ✅ Récupérer toutes les inscriptions
    @GetMapping
    public ResponseEntity<List<InscriptionStudentDTO>> getAll() {
        List<InscriptionStudentDTO> inscriptions = inscriptionService.getAll()
                .stream()
                .map(InscriptionStudentMapper::toDTO) // mapper propre
                .toList();
        return ResponseEntity.ok(inscriptions);
    }

    // ✅ Récupérer une inscription par ID
    @GetMapping("/{id}")
    public ResponseEntity<InscriptionStudentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                InscriptionStudentMapper.toDTO(inscriptionService.getById(id))
        );
    }

    // ✅ Récupérer une inscription par studentId et classId
    @GetMapping("/by-student-and-class")
    public ResponseEntity<InscriptionStudentDTO> getByStudentAndClass(
            @RequestParam Long studentId,
            @RequestParam Long classeRoomId) {
        return ResponseEntity.ok(
                InscriptionStudentMapper.toDTO(inscriptionService.getByStudentAndClass(studentId, classeRoomId)));
    }
    
    @GetMapping("/{classId}")
    public ResponseEntity<Long> getCountByClass(@PathVariable Long classeRoomId) {
        Long total = inscriptionService.getCountByClass(classeRoomId);
        return ResponseEntity.ok(total);
    }

    // ✅ Supprimer une inscription
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

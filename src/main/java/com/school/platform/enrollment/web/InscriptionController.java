package com.school.platform.enrollment.web;

import com.school.platform.academic.application.dto.ClasseRoomStudentCountDTO;
import com.school.platform.enrollment.application.dto.InscriptionStudentDTO;
import com.school.platform.academic.application.dto.SectionStudentCountDTO;
import com.school.platform.enrollment.application.InscriptionStudentService;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
// import com.school.platform.enrollment.application.InscriptionStudentServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inscription")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionStudentService inscriptionService;
    

    // ✅ Créer une inscription
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<InscriptionStudentDTO> createInscription(@Valid @RequestBody  InscriptionStudentDTO request) {
        // InscriptionStudentDTO inscription = inscriptionService.createInscription(
        //         request.getStudent(),
        //         request.getClassId(),
        //         request.getMontantId(),
        //         request.getAnneeScolaireId()
        // );
        return ResponseEntity.ok(inscriptionService.createInscription(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<?> updateInscription(@PathVariable Long id, @RequestBody InscriptionStudentDTO dto){
        try{
            InscriptionStudentDTO updateInscriptionStudent =inscriptionService.updateInscription(id, dto);
            return ResponseEntity.ok(updateInscriptionStudent);
        }catch(ResourceNotFoundException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    // ✅ Récupérer toutes les inscriptions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<InscriptionStudentDTO>> getAll() {
        // List<InscriptionStudentDTO> inscriptions = inscriptionService.getAll()
        //         .stream()
        //         .map(InscriptionStudentMapper::toDTO) // mapper propre
        //         .toList();
        return ResponseEntity.ok(inscriptionService.getAll());
    }

    @GetMapping("/by-class/{classeRoomId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<InscriptionStudentDTO>> getByClass(
            @PathVariable Long classeRoomId,
            @RequestParam(required = false) Long anneeScolaireId) {
        return ResponseEntity.ok(inscriptionService.getByClass(classeRoomId, anneeScolaireId));
    }

    // ✅ Récupérer une inscription par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<InscriptionStudentDTO> getById(@PathVariable Long id) {
        // return ResponseEntity.ok(
        //         InscriptionStudentMapper.toDto(inscriptionService.getById(id))
        // );
        return ResponseEntity.ok(inscriptionService.getById(id));
    }

    // ✅ Récupérer une inscription par studentId et classId
    @GetMapping("/by-student-and-class")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<InscriptionStudentDTO> getByStudentAndClass(
            @RequestParam Long studentId,
            @RequestParam Long classeRoomId) {
        return ResponseEntity.ok(inscriptionService.getByStudentAndClass(studentId, classeRoomId));
        // return ResponseEntity.ok(
        //         InscriptionStudentMapper.toDTO(inscriptionService.getByStudentAndClass(studentId, classeRoomId)));
    }

  @GetMapping("/count/{classeRoomId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Long> getCountByClass(@PathVariable Long classeRoomId) {
        long total = inscriptionService.getCountByClass(classeRoomId);
        return ResponseEntity.ok(total);
    }
    
    // @GetMapping("/{classId}")
    // public ResponseEntity<Long> getCountByClass(@PathVariable Long classeRoomId) {
    //     Long total = inscriptionService.getCountByClass(classeRoomId);
    //     return ResponseEntity.ok(total);
    // }

    // ✅ Supprimer une inscription
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

      @GetMapping("/count-by-section")
      @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
      public ResponseEntity<List<SectionStudentCountDTO>> getStudentCountBySection() {
          List<SectionStudentCountDTO> counts = inscriptionService.getStudentCountBySection();
          return ResponseEntity.ok(counts);
      }
    
      @GetMapping("/count-by-classeRoom")
      @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
      public ResponseEntity<List<ClasseRoomStudentCountDTO>> getStudentcountByClasse() {
          List<ClasseRoomStudentCountDTO> counts = inscriptionService.getStudentCountByClasse();
          return ResponseEntity.ok(counts);
      }

}

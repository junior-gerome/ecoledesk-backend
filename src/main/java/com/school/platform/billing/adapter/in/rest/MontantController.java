package com.school.platform.billing.adapter.in.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.school.platform.billing.application.MontantService;
import com.school.platform.billing.application.dto.MontantResponse;
import com.school.platform.billing.domain.model.Montant;

@RestController
@RequestMapping("/montant")
public class MontantController {
  private final MontantService montantService;

  public MontantController(MontantService montantService){
    this.montantService=montantService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<MontantResponse> createMontant(@RequestBody Montant montant){
    Montant createdMontant = montantService.createMontant(montant);
    return ResponseEntity.status(HttpStatus.CREATED).body(MontantResponse.from(createdMontant));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<List<MontantResponse>> getAllMontant(){
    List<MontantResponse> montants = montantService.getAllMontant().stream()
        .map(MontantResponse::from)
        .toList();
    return ResponseEntity.ok(montants);

  }

  @GetMapping("/by-class-and-type")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<MontantResponse> getByClassAndType(@RequestParam("classeRoomId") Long classeRoomId, @RequestParam("typePaiement") String typePaiement){
    Montant montant = montantService.getByClasseAndType(classeRoomId, typePaiement);
    return ResponseEntity.ok(MontantResponse.from(montant));
  }

  @GetMapping("/preinscription/class/{classeRoomId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<MontantResponse> getPreinscriptionByClassId(@PathVariable Long classeRoomId){
    Montant montant = montantService.getPreinscriptionByClasse(classeRoomId);
    return ResponseEntity.ok(MontantResponse.from(montant));
  }

  @GetMapping("/class/{classeRoomId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<MontantResponse> getByClassId(@PathVariable Long classeRoomId){
    Montant montant = montantService.getByClasse(classeRoomId);
    return ResponseEntity.ok(MontantResponse.from(montant));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<MontantResponse> getMontantById(@PathVariable Long id){
    return ResponseEntity.ok(MontantResponse.from(montantService.getMontantById(id)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<MontantResponse> updateMontant(@PathVariable Long id, @RequestBody Montant montant){
    return ResponseEntity.ok(MontantResponse.from(montantService.updateMontant(id, montant)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Void> deleteMontant(@PathVariable Long id){
    montantService.deleteMontant(id);
    return ResponseEntity.noContent().build();
  }

}
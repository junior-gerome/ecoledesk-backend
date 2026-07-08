package com.school.platform.billing.adapter.in.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.school.platform.billing.domain.model.Montant;
import com.school.platform.billing.application.MontantService;

@RestController
@RequestMapping("/montant")
public class MontantController {
  private final MontantService montantService;

  public MontantController(MontantService montantService){
    this.montantService=montantService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Montant> createMontant(@RequestBody Montant montant){
    Montant createdMontant = montantService.createMontant(montant);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMontant);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<List<Montant>> getAllMontant(){
    List<Montant> montants = montantService.getAllMontant();
    return ResponseEntity.ok(montants);

  }

  @GetMapping("/by-class-and-type")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Montant> getByClassAndType(@RequestParam("classeRoomId") Long classeRoomId, @RequestParam("typePaiement") String typePaiement){
    Montant montant = montantService.getByClasseAndType(classeRoomId, typePaiement);
    return ResponseEntity.ok(montant);
  }

  @GetMapping("/preinscription/class/{classeRoomId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Montant> getPreinscriptionByClassId(@PathVariable Long classeRoomId){
    Montant montant = montantService.getPreinscriptionByClasse(classeRoomId);
    return ResponseEntity.ok(montant);
  }

  @GetMapping("/class/{classeRoomId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Montant> getByClassId(@PathVariable Long classeRoomId){
    Montant montant = montantService.getByClasse(classeRoomId);
    return ResponseEntity.ok(montant);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Montant> getMontantById(@PathVariable Long id){
    return ResponseEntity.ok(montantService.getMontantById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Montant> updateMontant(@PathVariable Long id, @RequestBody Montant montant){
    return ResponseEntity.ok(montantService.updateMontant(id, montant));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Void> deleteMontant(@PathVariable Long id){
    montantService.deleteMontant(id);
    return ResponseEntity.noContent().build();
  }

}

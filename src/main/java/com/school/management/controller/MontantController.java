package com.school.management.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.school.management.model.Montant;
import com.school.management.service.MontantService;
import com.school.management.enums.TypePaiement;

@RestController
@RequestMapping("/montant")
@CrossOrigin(origins = "*")
public class MontantController {
  private final MontantService montantService;

  public MontantController(MontantService montantService){
    this.montantService=montantService;
  }

  @PostMapping
  public ResponseEntity<Montant> createMontant(@RequestBody Montant montant){
    Montant createdMontant = montantService.createMontant(montant);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMontant);
  }

  @GetMapping
  public ResponseEntity<List<Montant>> getAllMontant(){
    List<Montant> montants = montantService.getAllMontant();
    return ResponseEntity.ok(montants);

  }

  @GetMapping("/by-class-and-type")
  public ResponseEntity<Montant> getByClassAndType(@RequestParam("classeRoomId") Long classeRoomId, @RequestParam("typePaiement") String typePaiement){
    Montant montant = montantService.getByClasseAndType(classeRoomId, typePaiement);
    return ResponseEntity.ok(montant);
  }

  @GetMapping("/classId")
  public ResponseEntity<Montant> getByClassId(@PathVariable Long classeRoomId){
    Montant montant = montantService.getByClasse(classeRoomId);
    return ResponseEntity.ok(montant);
  }

}

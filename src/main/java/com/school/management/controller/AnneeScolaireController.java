package com.school.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.school.management.dto.AnneeScolaireDTO;
import com.school.management.model.AnneeScolaire;
import com.school.management.service.AnneeScolaireService;



@RestController
@RequestMapping("/annees-scolaires")
@CrossOrigin(origins = "*")
public class AnneeScolaireController {
  private final AnneeScolaireService anneeScolaireService;

  public AnneeScolaireController(AnneeScolaireService anneeScolaireService) {
    this.anneeScolaireService = anneeScolaireService;
  }
  @PostMapping
  public ResponseEntity<AnneeScolaireDTO> createAnneeScolaire(@RequestBody AnneeScolaireDTO dto) {
    AnneeScolaireDTO createdAnneeScolaire = anneeScolaireService.createAnneeScolaire(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAnneeScolaire);
  }


  @GetMapping
  public List<AnneeScolaireDTO> getAllAnneeScolaire() {
    return anneeScolaireService.getAllAnneeScolaire();
  }
  
  @GetMapping("id")
  public ResponseEntity<AnneeScolaireDTO> getById(@PathVariable Long id) {
    AnneeScolaireDTO annee = anneeScolaireService.getById(id);
    return ResponseEntity.ok(annee);
  }
  
    
  @GetMapping("/active")
  public ResponseEntity<AnneeScolaireDTO> getActiveAnneeScolaire() {
    AnneeScolaireDTO activeAnneeScolaire = anneeScolaireService.getActiveAnneeScolaire();
    return ResponseEntity.ok(activeAnneeScolaire);
  }

  @PutMapping("/{id}/activate")
  public ResponseEntity<AnneeScolaire> activateAnneeScolaire(@PathVariable Long id) {
    AnneeScolaire activatedAnneeScolaire = anneeScolaireService.activateAnneeScolaire(id);
    return ResponseEntity.ok(activatedAnneeScolaire);
  }

  @PutMapping("/{id}")
  public ResponseEntity<AnneeScolaireDTO> getAnneeScolaireById(@RequestParam Long id) {
    AnneeScolaireDTO anneeScolaire = anneeScolaireService.updateAnneeScolaire(id, null);
    return ResponseEntity.ok(anneeScolaire);
  }

  @PutMapping("/{id}/inactivate")
  public ResponseEntity<AnneeScolaire> inactivateAnneeScolaire(@PathVariable Long id) {
    AnneeScolaire inactivatedAnneeScolaire = anneeScolaireService.inactivateAnneeScolaire(id);
    return ResponseEntity.ok(inactivatedAnneeScolaire);
  }

 



}

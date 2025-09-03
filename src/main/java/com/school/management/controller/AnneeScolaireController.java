package com.school.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.management.model.AnneeScolaire;
import com.school.management.service.AnneeScolaireService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/annees-scolaires")
@CrossOrigin(origins = "*")
public class AnneeScolaireController {
  private final AnneeScolaireService anneeScolaireService;

  public AnneeScolaireController(AnneeScolaireService anneeScolaireService) {
    this.anneeScolaireService = anneeScolaireService;
  }
  @PostMapping
  public ResponseEntity<AnneeScolaire> createAnneeScolaire(@RequestBody AnneeScolaire anneeScolaire) {
    AnneeScolaire createdAnneeScolaire = anneeScolaireService.createAnneeScolaire(anneeScolaire);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAnneeScolaire);
  }


  @GetMapping
  public List<AnneeScolaire> getAllAnneeScolaire() {
    return anneeScolaireService.getAllAnneeScolaire();  
  }
  
    
  @GetMapping("/active")
  public ResponseEntity<AnneeScolaire> getActiveAnneeScolaire() {
    AnneeScolaire activeAnneeScolaire = anneeScolaireService.getActiveAnneeScolaire();
    return ResponseEntity.ok(activeAnneeScolaire);
  }

  @PutMapping("/{id}/activate")
  public ResponseEntity<AnneeScolaire> activateAnneeScolaire(@PathVariable Long id) {
    AnneeScolaire activatedAnneeScolaire = anneeScolaireService.activateAnneeScolaire(id);
    return ResponseEntity.ok(activatedAnneeScolaire);
  }

  @PutMapping("/{id}")
  public ResponseEntity<AnneeScolaire> getAnneeScolaireById(@RequestParam Long id) {
    AnneeScolaire anneeScolaire = anneeScolaireService.updateAnneeScolaire(id, null);
    return ResponseEntity.ok(anneeScolaire);
  }

  @PutMapping("/{id}/inactivate")
  public ResponseEntity<AnneeScolaire> inactivateAnneeScolaire(@PathVariable Long id) {
    AnneeScolaire inactivatedAnneeScolaire = anneeScolaireService.inactivateAnneeScolaire(id);
    return ResponseEntity.ok(inactivatedAnneeScolaire);
  }

 



}

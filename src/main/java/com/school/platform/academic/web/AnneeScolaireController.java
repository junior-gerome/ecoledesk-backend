package com.school.platform.academic.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.school.platform.academic.application.dto.AnneeScolaireDTO;
import com.school.platform.academic.domain.model.AnneeScolaire;
import com.school.platform.academic.application.AnneeScolaireService;



@RestController
@RequestMapping("/annees-scolaires")
public class AnneeScolaireController {
  private final AnneeScolaireService anneeScolaireService;

  public AnneeScolaireController(AnneeScolaireService anneeScolaireService) {
    this.anneeScolaireService = anneeScolaireService;
  }
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<AnneeScolaireDTO> createAnneeScolaire(@RequestBody AnneeScolaireDTO dto) {
    AnneeScolaireDTO createdAnneeScolaire = anneeScolaireService.createAnneeScolaire(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdAnneeScolaire);
  }


  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public List<AnneeScolaireDTO> getAllAnneeScolaire() {
    return anneeScolaireService.getAllAnneeScolaire();
  }
  
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<AnneeScolaireDTO> getById(@PathVariable Long id) {
    AnneeScolaireDTO annee = anneeScolaireService.getById(id);
    return ResponseEntity.ok(annee);
  }
  
    
  @GetMapping("/active")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<AnneeScolaireDTO> getActiveAnneeScolaire() {
    AnneeScolaireDTO activeAnneeScolaire = anneeScolaireService.getActiveAnneeScolaire();
    return ResponseEntity.ok(activeAnneeScolaire);
  }

  @PutMapping("/{id}/activate")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<AnneeScolaire> activateAnneeScolaire(@PathVariable Long id) {
    AnneeScolaire activatedAnneeScolaire = anneeScolaireService.activateAnneeScolaire(id);
    return ResponseEntity.ok(activatedAnneeScolaire);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<AnneeScolaireDTO> updateAnneeScolaire(@PathVariable Long id, @RequestBody AnneeScolaireDTO dto) {
    AnneeScolaireDTO anneeScolaire = anneeScolaireService.updateAnneeScolaire(id, dto);
    return ResponseEntity.ok(anneeScolaire);
  }

  @PutMapping("/{id}/inactivate")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<AnneeScolaire> inactivateAnneeScolaire(@PathVariable Long id) {
    AnneeScolaire inactivatedAnneeScolaire = anneeScolaireService.inactivateAnneeScolaire(id);
    return ResponseEntity.ok(inactivatedAnneeScolaire);
  }

 



}

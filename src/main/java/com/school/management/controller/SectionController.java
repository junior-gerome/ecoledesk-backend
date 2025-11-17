package com.school.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.management.dto.SectionDTO;
import com.school.management.service.SectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/section")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SectionController {
  private final SectionService sectionService;
  
  @PostMapping
  public ResponseEntity<SectionDTO> createSection(@Valid @RequestBody SectionDTO dto) {
    SectionDTO savedSection = sectionService.createSection(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedSection);
  }
  @GetMapping("/{id}")
  public ResponseEntity<SectionDTO> getSectionById(@PathVariable Long id) {
    SectionDTO section = sectionService.getSectionById(id);
    return ResponseEntity.ok(section);
  }
@GetMapping("/libelle/{libelle}")
  public ResponseEntity<SectionDTO> getSectionByLibelle(@PathVariable String libelle) {
    SectionDTO section = sectionService.getSectionByLibelle(libelle);
    return ResponseEntity.ok(section);
  }
  @PutMapping("/{id}")
  public ResponseEntity<SectionDTO> updateSection(@PathVariable Long id, @RequestBody SectionDTO updatedSection) {
    SectionDTO section = sectionService.updateSection(id, updatedSection);
    return ResponseEntity.ok(section);
  }
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
    sectionService.deleteSection(id);
    return ResponseEntity.noContent().build();
  }
  @GetMapping
  public ResponseEntity<List<SectionDTO>> getAllSections() {
    List<SectionDTO> sections = sectionService.getAllSections();
    return ResponseEntity.ok(sections);
  }
  

}

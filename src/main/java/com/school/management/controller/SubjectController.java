package com.school.management.controller;

import java.util.List;


import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.school.management.dto.SubjectDTO;
import com.school.management.service.SubjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubjectController {

  private final SubjectService subjectService;

  @PostMapping
  public ResponseEntity<SubjectDTO> createSubject(@Valid @RequestBody SubjectDTO dto) {
    SubjectDTO entity = subjectService.createdSubject(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(entity);
  }

  @GetMapping
  public ResponseEntity<List<SubjectDTO>> getAll(){
    return ResponseEntity.ok(subjectService.getAllSubject());
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<SubjectDTO> getById(@PathVariable Long id){
    return ResponseEntity.ok(subjectService.getSubjectById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    subjectService.deleteSubject(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<SubjectDTO> update(@PathVariable Long id, @RequestBody SubjectDTO dto){
    SubjectDTO updated = subjectService.updatedSubject(id, dto);
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/count")
  public ResponseEntity<Long> getTotalSubject(){
    Long total = subjectService.getTotalSubjects();
    return ResponseEntity.ok(total);
  }
}

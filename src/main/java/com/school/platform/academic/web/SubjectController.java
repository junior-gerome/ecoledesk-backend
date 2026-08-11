package com.school.platform.academic.web;

import java.util.List;


import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.school.platform.academic.application.dto.SubjectDTO;
import com.school.platform.academic.application.interfaces.SubjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
public class SubjectController {

  private final SubjectService subjectService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<SubjectDTO> createSubject(@Valid @RequestBody SubjectDTO dto) {
    SubjectDTO entity = subjectService.createdSubject(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(entity);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<List<SubjectDTO>> getAll(){
    return ResponseEntity.ok(subjectService.getAllSubject());
  }
  
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<SubjectDTO> getById(@PathVariable Long id){
    return ResponseEntity.ok(subjectService.getSubjectById(id));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    subjectService.deleteSubject(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<SubjectDTO> update(@PathVariable Long id, @RequestBody SubjectDTO dto){
    SubjectDTO updated = subjectService.updatedSubject(id, dto);
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/count")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Long> getTotalSubject(){
    Long total = subjectService.getTotalSubjects();
    return ResponseEntity.ok(total);
  }
}

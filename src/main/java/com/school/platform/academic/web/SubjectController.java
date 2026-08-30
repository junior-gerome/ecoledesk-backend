package com.school.platform.academic.web;

import java.util.List;


import com.school.platform.academic.application.dto.subject.SubjectReponse;
import com.school.platform.academic.application.dto.subject.SubjectRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<SubjectRequest> createSubject(@Valid @RequestBody SubjectRequest dto) {
    SubjectRequest entity = subjectService.createdSubject(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(entity);
  }

  /**
   * Générer un code pour une matière.
   *
   * Exemple :
   *
   * GET /subject/generate-code?nameSubject=Mathématiques.
   *
   * Réponse :
   *
   * MATH
   */
  @GetMapping("/generate-code")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<String> generateSubjectCode(
          @RequestParam String nameSubject) {

    String code = subjectService.generateSubjectCode(nameSubject);

    return ResponseEntity.ok(code);
  }


  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<List<SubjectReponse>> getAll(){
    return ResponseEntity.ok(subjectService.getAllSubject());
  }
  
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<SubjectReponse> getById(@PathVariable Long id){
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
  public ResponseEntity<SubjectReponse> update(@PathVariable Long id, @RequestBody SubjectReponse dto){
    SubjectReponse updated = subjectService.updatedSubject(id, dto);
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/count")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Long> getTotalSubject(){
    Long total = subjectService.getTotalSubjects();
    return ResponseEntity.ok(total);
  }
}

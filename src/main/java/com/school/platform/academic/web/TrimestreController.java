package com.school.platform.academic.web;

import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.school.platform.academic.application.dto.TrimestreDTO;
import com.school.platform.academic.application.TrimestreService;

//import jakarta.validation.Valid;  


@RestController
@RequestMapping("/trimestre")
@RequiredArgsConstructor
public class TrimestreController {
  private final TrimestreService trimestreService;

@PostMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
public ResponseEntity<TrimestreDTO> createTrimestre(@RequestBody TrimestreDTO dto) {
  TrimestreDTO entity = trimestreService.createTrimestre(dto);
  return ResponseEntity.status(HttpStatus.CREATED).body(entity);
}
  

@GetMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<List<TrimestreDTO>> getAll() {
    List<TrimestreDTO> trimestre = trimestreService.getALLTrimestre();
    return ResponseEntity.ok(trimestre);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<TrimestreDTO> update(@PathVariable Long id, @RequestBody TrimestreDTO dto) {
    TrimestreDTO updated = trimestreService.updateTrimestre(id, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(updated);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteTrimestre(@PathVariable Long id) {
        trimestreService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<TrimestreDTO> getTrimestreById(@PathVariable Long id) {
   TrimestreDTO trimestre = trimestreService.getTrimestreById(id);
   return ResponseEntity.ok(trimestre);
  }

  @GetMapping("/count")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Long> getTotalTrimestre() {
    Long total = trimestreService.getTotalTrimestre();
    return ResponseEntity.ok(total);
  }
}

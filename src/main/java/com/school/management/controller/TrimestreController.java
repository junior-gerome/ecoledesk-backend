package com.school.management.controller;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.school.management.dto.TrimestreDTO;
import com.school.management.service.TrimestreService;

//import jakarta.validation.Valid;  


@RestController
@RequestMapping("/trimestre")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class TrimestreController {
  private final TrimestreService trimestreService;

@PostMapping
public ResponseEntity<TrimestreDTO> createTrimestre(@RequestBody TrimestreDTO dto) {
  TrimestreDTO entity = trimestreService.createTrimestre(dto);
  return ResponseEntity.status(HttpStatus.CREATED).body(entity);
}
  

@GetMapping
  public ResponseEntity<List<TrimestreDTO>> getAll() {
    List<TrimestreDTO> trimestre = trimestreService.getALLTrimestre();
    return ResponseEntity.ok(trimestre);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TrimestreDTO> update(@PathVariable Long id, @RequestBody TrimestreDTO dto) {
    TrimestreDTO updated = trimestreService.updateTrimestre(id, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTrimestre(@PathVariable Long id) {
        trimestreService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<TrimestreDTO> getTrimestreById(@PathVariable Long id) {
   TrimestreDTO trimestre = trimestreService.getTrimestreById(id);
   return ResponseEntity.ok(trimestre);
  }

  @GetMapping("/count")
  public ResponseEntity<Long> getTotalTrimestre() {
    Long total = trimestreService.getTotalTrimestre();
    return ResponseEntity.ok(total);
  }
}
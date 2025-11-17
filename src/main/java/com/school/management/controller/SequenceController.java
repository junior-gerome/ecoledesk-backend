package com.school.management.controller;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.school.management.dto.SequenceDTO;
import com.school.management.service.SequenceService;
  


@RestController
@RequestMapping("/sequence")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class SequenceController {
  private final SequenceService sequenceService;

@PostMapping
public ResponseEntity<SequenceDTO> createSequence(@RequestBody SequenceDTO dto){
  SequenceDTO created = sequenceService.createSequence(dto);
  return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
  

@GetMapping
  public ResponseEntity<List<SequenceDTO>> getAllSequence() {
    List<SequenceDTO> sequence = sequenceService.getALLSequence();
    return ResponseEntity.ok(sequence);
  }

  @PutMapping("/{id}")
  public ResponseEntity<SequenceDTO> update(@PathVariable Long id, @RequestBody SequenceDTO dto) {
    SequenceDTO updated = sequenceService.updateSequence(id, dto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSequence(@PathVariable Long id) {
        sequenceService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<SequenceDTO> getSequenceById(@PathVariable Long id) {
     SequenceDTO sequence = sequenceService.getSequenceById(id);
    return ResponseEntity.ok(sequence);
  }
  
  @GetMapping("/count")
  public ResponseEntity<Long> getTotalSequence() {
    Long total = sequenceService.getTotalSequence();
      return ResponseEntity.ok(total);
  }
  

}
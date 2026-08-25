package com.school.platform.academic.web;

import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.school.platform.academic.application.dto.sequence.SequenceDTO;
import com.school.platform.academic.application.interfaces.SequenceService;
  


@RestController
@RequestMapping("/sequence")
@RequiredArgsConstructor
public class SequenceController {
  private final SequenceService sequenceService;

@PostMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
public ResponseEntity<SequenceDTO> createSequence(@RequestBody SequenceDTO dto){
  SequenceDTO created = sequenceService.createSequence(dto);
  return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
  

@GetMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<List<SequenceDTO>> getAllSequence() {
    List<SequenceDTO> sequence = sequenceService.getALLSequence();
    return ResponseEntity.ok(sequence);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<SequenceDTO> update(@PathVariable Long id, @RequestBody SequenceDTO dto) {
    SequenceDTO updated = sequenceService.updateSequence(id, dto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteSequence(@PathVariable Long id) {
        sequenceService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<SequenceDTO> getSequenceById(@PathVariable Long id) {
     SequenceDTO sequence = sequenceService.getSequenceById(id);
    return ResponseEntity.ok(sequence);
  }
  
  @GetMapping("/count")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<Long> getTotalSequence() {
    Long total = sequenceService.getTotalSequence();
      return ResponseEntity.ok(total);
  }
  

}

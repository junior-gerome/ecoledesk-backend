package com.school.management.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.school.management.dto.ParentDTO;
import com.school.management.service.ParentService;  


@RestController
@RequestMapping("/parents")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class ParentController {
  private final ParentService parentService;

  

  @PostMapping
  public ResponseEntity<ParentDTO> createParent(@RequestBody ParentDTO dto) {
    ParentDTO createdParent = parentService.createParent(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdParent);
  }

  @GetMapping
  public ResponseEntity<List<ParentDTO>> getAllParents() {
    List<ParentDTO> parents = parentService.getAllParents();
    return ResponseEntity.ok(parents);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ParentDTO> updateParent(@PathVariable Long id, @RequestBody ParentDTO dto) {
    ParentDTO updatedParent = parentService.updateParent(id, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(updatedParent);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ParentDTO> getParentById(@PathVariable Long id) {
   ParentDTO parents = parentService.getParentById(id);
   return ResponseEntity.ok(parents);
  }
}

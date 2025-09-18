package com.school.management.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import com.school.management.model.Parent;
import com.school.management.service.ParentService;  


@RestController
@RequestMapping("/parents")
@CrossOrigin(origins = "*") 
public class ParentController {
  private final ParentService parentService;

  public ParentController(ParentService parentService) {
    this.parentService = parentService;
  }

  @PostMapping
  public ResponseEntity<Parent> createParent(@RequestBody Parent parent) {
    Parent createdParent = parentService.createParent(parent);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdParent);
  }

  @GetMapping
  public ResponseEntity<List<Parent>> getAllParents() {
    List<Parent> parents = parentService.getAllParents();
    return ResponseEntity.ok(parents);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Parent> updateParent(@PathVariable Long id, @RequestBody Parent parent) {
    Parent updatedParent = parentService.updateParent(id, parent);
    return ResponseEntity.status(HttpStatus.CREATED).body(updatedParent);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Parent> getParentById(@PathVariable Long id) {
   Parent parents = parentService.getParentById(id);
   return ResponseEntity.ok(parents);
  }
}

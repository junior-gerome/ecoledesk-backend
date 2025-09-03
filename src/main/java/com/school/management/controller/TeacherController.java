package com.school.management.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.school.management.dto.TeacherDTO;
import com.school.management.service.TeacherService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/teachers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<?> createTeacher(@Valid @RequestBody TeacherDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        
        try {
            TeacherDTO createdTeacher = teacherService.createEnseignant(dto);
            return ResponseEntity.ok(createdTeacher);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherDTO teacherDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        
        try {
            TeacherDTO updatedTeacher = teacherService.updateEnseignant(id, teacherDTO);
            return ResponseEntity.ok(updatedTeacher);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDTO> getTeacherById(@PathVariable @Min(1) Long id) {
        TeacherDTO teacher = teacherService.getEnseignantById(id);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<TeacherDTO> teachers = teacherService.getAllEnseignants();
        return ResponseEntity.ok(teachers);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnseignant(@PathVariable Long id) {
        teacherService.deleteEnseignant(id);
        return ResponseEntity.noContent().build();
    }
}
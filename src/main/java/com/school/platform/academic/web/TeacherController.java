package com.school.platform.academic.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.school.platform.academic.domain.model.Affectation;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.infrastructure.persistence.AffectationRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.application.dto.TeacherDTO;
import com.school.platform.academic.application.interfaces.TeacherService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final AffectationRepository affectationRepository;
    private final ClasseRoomRepository classeRoomRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<TeacherDTO> getTeacherById(@PathVariable @Min(1) Long id) {
        TeacherDTO teacher = teacherService.getEnseignantById(id);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<TeacherDTO> teachers = teacherService.getAllEnseignants();
        return ResponseEntity.ok(teachers);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnseignant(@PathVariable Long id) {
        teacherService.deleteEnseignant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Long> getTotalTeachers() {
       Long total = teacherService.getTotalTeachers();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<TeacherDTO>> getAvailableTeachers() {
        Set<Long> assignedTeacherIds = classeRoomRepository.findAllWithDetails().stream()
            .map(ClasseRoom::getTeacher)
            .filter(teacher -> teacher != null && teacher.getId() != null)
            .map(teacher -> teacher.getId())
            .collect(Collectors.toSet());

        List<TeacherDTO> availableTeachers = teacherService.getAllEnseignants().stream()
            .filter(teacher -> teacher.getId() != null && !assignedTeacherIds.contains(teacher.getId()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(availableTeachers);
    }

    @GetMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<Map<String, Object>>> getTeacherSubjects(@RequestParam(required = false) Long teacherId) {
        List<Affectation> affectations = teacherId == null
            ? affectationRepository.findAll()
            : affectationRepository.findByTeacherId(teacherId);

        List<Map<String, Object>> rows = affectations.stream()
            .map(affectation -> Map.<String, Object>of(
                "teacherId", affectation.getTeacher().getId(),
                "teacherName", affectation.getTeacher().getFirstnameTeacher() + " " + affectation.getTeacher().getLastnameTeacher(),
                "subjectId", affectation.getSubject().getId(),
                "subjectName", affectation.getSubject().getNameSubject(),
                "classId", affectation.getClasse().getId(),
                "className", affectation.getClasse().getNameClasse(),
                "schoolYear", affectation.getAcademicYear().getLibelleAcademicYear()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<Map<String, Object>>> getTeacherSchedule(@RequestParam(required = false) Long teacherId) {
        List<Affectation> affectations = teacherId == null
            ? affectationRepository.findAll()
            : affectationRepository.findByTeacherId(teacherId);

        List<Map<String, Object>> rows = affectations.stream()
            .map(this::scheduleFromAffectation)
            .collect(Collectors.toList());

        return ResponseEntity.ok(rows);
    }

    private Map<String, Object> scheduleFromAffectation(Affectation affectation) {
        Map<String, Object> row = new HashMap<>();
        row.put("day", null);
        row.put("startTime", null);
        row.put("endTime", null);
        row.put("teacherId", affectation.getTeacher().getId());
        row.put("teacherName", affectation.getTeacher().getFirstnameTeacher() + " " + affectation.getTeacher().getLastnameTeacher());
        row.put("subjectId", affectation.getSubject().getId());
        row.put("subjectName", affectation.getSubject().getNameSubject());
        row.put("classId", affectation.getClasse().getId());
        row.put("className", affectation.getClasse().getNameClasse());
        row.put("schoolYear", affectation.getAcademicYear().getLibelleAcademicYear());
        return row;
    }
}

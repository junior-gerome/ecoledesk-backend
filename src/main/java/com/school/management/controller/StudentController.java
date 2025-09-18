package com.school.management.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.*;
import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.StudentDTO;
import com.school.management.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody StudentDTO dto) {
        try {
            StudentDTO savedStudent = studentService.createStudent(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne : " + ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody StudentDTO dto) {
        try {
            StudentDTO updatedStudent = studentService.updateStudent(id, dto);
            return ResponseEntity.ok(updatedStudent);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents(); // correction ici : StudentDTO
        return ResponseEntity.ok(students);
    }

    // @GetMapping("/by-classe/{classeId}")
    // public ResponseEntity<Page<StudentDTO>> getStudentsByClasse(@PathVariable Long classeId, Pageable pageable) {
    //     Page<StudentDTO> studentsPage = studentService.getStudentsByClasse(classeId, pageable);
    //     return ResponseEntity.status(HttpStatus.OK).body(studentsPage);
    // }

    // @GetMapping("/count")
    // public ResponseEntity<Map<String, Long>> getStudentCount(){
    //     Map<String, Long> result = new HashMap<>();
    //     result.put("total", studentService.getTotalStudents());
    //     return ResponseEntity.ok(result);
    // }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalStudents() {
        Long total = studentService.getTotalStudents();
        return ResponseEntity.ok(total);
    }
}

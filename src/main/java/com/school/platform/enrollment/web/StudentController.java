package com.school.platform.enrollment.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.StudentCommandService;
import com.school.platform.enrollment.application.StudentExportService;
import com.school.platform.enrollment.application.StudentQueryService;
import com.school.platform.enrollment.application.StudentStatisticsService;
import com.school.platform.reporting.application.StudentReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentCommandService studentCommandService;
    private final StudentQueryService studentQueryService;
    private final StudentReportService studentReportService;
    private final StudentStatisticsService studentStatisticsService;
    private final StudentExportService studentExportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public StudentDTO createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        return studentCommandService.createStudentWithGuardian(studentDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDTO dto) {
        StudentDTO updatedStudent = studentCommandService.updateStudent(id, dto);
        return ResponseEntity.ok(updatedStudent);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        StudentDTO student = studentQueryService.findById(id);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentCommandService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentQueryService.findAllActive();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> exportStudentsExcel(@RequestParam(required = false, name = "q") String keyword) {
        byte[] content = studentExportService.generateStudentsExcel(keyword);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=liste-eleves.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<byte[]> exportStudentsPdf(@RequestParam(required = false, name = "q") String keyword) {
        byte[] content = studentExportService.generateStudentsPdf(keyword);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=liste-eleves.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Long> getTotalStudents() {
        Long total = studentQueryService.findAll(org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Map<String, Object>> getStudentStatistics() {
        return ResponseEntity.ok(studentStatisticsService.getStudentStatistics());
    }

    @GetMapping("/statistics/by-class")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<com.school.platform.academic.application.dto.classeroom.ClasseRoomStudentCountDTO>> getStudentStatisticsByClass() {
        return ResponseEntity.ok(studentStatisticsService.getStatsByClasse());
    }

    @GetMapping("/statistics/by-section")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<com.school.platform.academic.application.dto.section.SectionStudentCountDTO>> getStudentStatisticsBySection() {
        return ResponseEntity.ok(studentStatisticsService.getStatsBySection());
    }
}

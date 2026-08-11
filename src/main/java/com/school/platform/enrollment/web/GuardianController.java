package com.school.platform.enrollment.web;

import com.school.platform.enrollment.application.GuardianService;
import com.school.platform.enrollment.application.StudentService;
import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.notification.domain.model.Notification;
import com.school.platform.notification.infrastructure.persistence.NotificationRepository;
import com.school.platform.shared.web.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guardians")
@RequiredArgsConstructor
public class GuardianController {
    private final GuardianService guardianService;
    private final StudentService studentService;
    private final NotificationRepository notificationRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GuardianDTO> createGuardian(@RequestBody GuardianDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guardianService.createGuardian(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<PageResponse<GuardianDTO>> getAllGuardians(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(PageResponse.from(guardianService.getAllGuardians(pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GuardianDTO> updateGuardian(@PathVariable Long id, @RequestBody GuardianDTO dto) {
        return ResponseEntity.ok(guardianService.updateGuardian(id, dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<GuardianDTO> patchGuardian(@PathVariable Long id, @RequestBody GuardianDTO dto) {
        return ResponseEntity.ok(guardianService.updateGuardian(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuardian(@PathVariable Long id) {
        guardianService.deleteGuardian(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<GuardianDTO> getGuardianById(@PathVariable Long id) {
        return ResponseEntity.ok(guardianService.getGuardianById(id));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<Map<String, Object>>> getLinkedStudents(@PathVariable Long id) {
        List<Map<String, Object>> students = studentService.getAllStudents().stream()
                .filter(student -> student.getGuardian() != null && id.equals(student.getGuardian().getId()))
                .map(this::toLinkedStudent)
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<Map<String, Object>> sendMessage(@PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String message = payload.getOrDefault("message", "").trim();
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le message est obligatoire"));
        }
        Notification saved = notificationRepository.save(new Notification(id, message));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(), "guardianId", id, "message", saved.getMessage(), "createdAt", saved.getCreatedAt()));
    }

    private Map<String, Object> toLinkedStudent(StudentDTO student) {
        return Map.of("studentId", student.getId(),
                "studentName", student.getFirstNameStudent() + " " + student.getLastNameStudent(),
                "dateOfBirth", student.getDateOfBirth(), "gender", student.getGender());
    }
}

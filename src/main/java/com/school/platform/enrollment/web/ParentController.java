package com.school.platform.enrollment.web;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import com.school.platform.enrollment.application.dto.ParentDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.notification.domain.model.Notification;
import com.school.platform.notification.infrastructure.persistence.NotificationRepository;
import com.school.platform.enrollment.application.ParentService;  
import com.school.platform.enrollment.application.StudentService;
import com.school.platform.shared.web.PageResponse;


@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
public class ParentController {
  private final ParentService parentService;
  private final StudentService studentService;
  private final NotificationRepository notificationRepository;

  

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<ParentDTO> createParent(@RequestBody ParentDTO dto) {
    ParentDTO createdParent = parentService.createParent(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdParent);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<PageResponse<ParentDTO>> getAllParents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    return ResponseEntity.ok(PageResponse.from(parentService.getAllParents(pageable)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<ParentDTO> updateParent(@PathVariable Long id, @RequestBody ParentDTO dto) {
    ParentDTO updatedParent = parentService.updateParent(id, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(updatedParent);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
  public ResponseEntity<ParentDTO> patchParent(@PathVariable Long id, @RequestBody ParentDTO dto) {
    return ResponseEntity.ok(parentService.updateParent(id, dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<ParentDTO> getParentById(@PathVariable Long id) {
   ParentDTO parents = parentService.getParentById(id);
   return ResponseEntity.ok(parents);
  }

  @GetMapping("/{id}/students")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<List<Map<String, Object>>> getLinkedStudents(@PathVariable Long id) {
    List<Map<String, Object>> students = studentService.getAllStudents().stream()
        .filter(student -> student.getParent() != null && id.equals(student.getParent().getId()))
        .map(this::toLinkedStudent)
        .collect(Collectors.toList());
    return ResponseEntity.ok(students);
  }

  private Map<String, Object> toLinkedStudent(StudentDTO student) {
    return Map.of(
        "studentId", student.getId(),
        "studentName", student.getFirstNameStudent() + " " + student.getLastNameStudent(),
        "dateOfBirth", student.getDateOfBirth(),
        "gender", student.getGender()
    );
  }

  @PostMapping("/{id}/messages")
  @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
  public ResponseEntity<Map<String, Object>> sendMessage(
      @PathVariable Long id,
      @RequestBody Map<String, String> payload) {
    String message = payload.getOrDefault("message", "").trim();
    if (message.isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("message", "Le message est obligatoire"));
    }
    Notification saved = notificationRepository.save(new Notification(id, message));
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
        "id", saved.getId(),
        "parentId", id,
        "message", saved.getMessage(),
        "createdAt", saved.getCreatedAt()
    ));
  }
}

package com.school.platform.academic.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.ClasseRoomDTO;
import com.school.platform.academic.application.dto.TeacherDTO;
import com.school.platform.academic.application.ClassRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassRoomController {

    private final ClassRoomService classRoomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<ClasseRoomDTO> createClassRoom(@RequestBody ClasseRoomDTO dto) {
        ClasseRoomDTO createdClassRoom = classRoomService.createClassRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClassRoom);
    }


    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<ClasseRoomDTO> getClassRoomById(@PathVariable Long id) {
        try {
           // ClasseRoom classRoom = classRoomService.getClassRoomById(id);
            return ResponseEntity.ok(classRoomService.getClassRoomById(id));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<?> updateClassRoom(@PathVariable Long id, @RequestBody ClasseRoomDTO classRoom) {
        try {
           // ClasseRoom updatedClassRoom = ;
            return ResponseEntity.ok(classRoomService.updateClassRoom(id, classRoom));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne : " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClassRoom(@PathVariable Long id) {
        try {
            classRoomService.deleteClassRoom(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<ClasseRoomDTO>> getAllClassRooms() {
       List<ClasseRoomDTO> classRooms = classRoomService.getAllClassRooms();
        return ResponseEntity.ok(classRooms);
    }

    @GetMapping("/by-section/{sectionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<ClasseRoomDTO>> getClassesBySection(@PathVariable Long sectionId) {
        // List<ClasseRoom> classRooms = classRoomService.getClassRoomsBySection(sectionId);
        return ResponseEntity.ok(classRoomService.getClassRoomsBySection(sectionId));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Long> getTotalClassRooms() {
        // Long total = classRoomService.getTotalClassRooms();
        return ResponseEntity.ok(classRoomService.getTotalClassRooms());
    }

    @GetMapping("/teachers/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<TeacherDTO>> getAvailableTeachers() {
        return ResponseEntity.ok(classRoomService.getAvailableTeachers());
    }

    @PostMapping("/{classId}/teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Void> assignTeacher(
            @PathVariable Long classId,
            @PathVariable Long teacherId) {
        classRoomService.assignTeacher(classId, teacherId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{classId}/teacher")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Void> removeTeacher(@PathVariable Long classId) {
        classRoomService.removeTeacher(classId);
        return ResponseEntity.noContent().build();
    }
}

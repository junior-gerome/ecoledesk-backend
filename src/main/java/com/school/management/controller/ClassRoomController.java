package com.school.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.school.exception.ResourceNotFoundException;
import com.school.management.model.ClasseRoom;
import com.school.management.service.ClassRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/classes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClassRoomController {

    private final ClassRoomService classRoomService;

    @PostMapping
    public ResponseEntity<ClasseRoom> createClassRoom(@RequestBody ClasseRoom classRoom) {
        ClasseRoom createdClassRoom = classRoomService.createClassRoom(classRoom);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClassRoom);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClasseRoom> getClassRoomById(@PathVariable Long id) {
        try {
            ClasseRoom classRoom = classRoomService.getClassRoomById(id);
            return ResponseEntity.ok(classRoom);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClassRoom(@PathVariable Long id, @RequestBody ClasseRoom classRoom) {
        try {
            ClasseRoom updatedClassRoom = classRoomService.updateClassRoom(id, classRoom);
            return ResponseEntity.ok(updatedClassRoom);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne : " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClassRoom(@PathVariable Long id) {
        try {
            classRoomService.deleteClassRoom(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ClasseRoom>> getAllClassRooms() {
        List<ClasseRoom> classRooms = classRoomService.getAllClassRooms();
        return ResponseEntity.ok(classRooms);
    }

    @GetMapping("/by-section/{sectionId}")
    public ResponseEntity<List<ClasseRoom>> getClassesBySection(@PathVariable Long sectionId) {
        List<ClasseRoom> classRooms = classRoomService.getClassRoomsBySection(sectionId);
        return ResponseEntity.ok(classRooms);
    }
}

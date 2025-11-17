package com.school.management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.ClasseRoomDTO;
import com.school.management.service.ClassRoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/classes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ClassRoomController {

    private final ClassRoomService classRoomService;

    @PostMapping
    public ResponseEntity<ClasseRoomDTO> createClassRoom(@RequestBody ClasseRoomDTO dto) {
        ClasseRoomDTO createdClassRoom = classRoomService.createClassRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClassRoom);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClasseRoomDTO> getClassRoomById(@PathVariable Long id) {
        try {
           // ClasseRoom classRoom = classRoomService.getClassRoomById(id);
            return ResponseEntity.ok(classRoomService.getClassRoomById(id));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<List<ClasseRoomDTO>> getAllClassRooms() {
       List<ClasseRoomDTO> classRooms = classRoomService.getAllClassRooms();
        return ResponseEntity.ok(classRooms);
    }

    @GetMapping("/by-section/{sectionId}")
    public ResponseEntity<List<ClasseRoomDTO>> getClassesBySection(@PathVariable Long sectionId) {
        // List<ClasseRoom> classRooms = classRoomService.getClassRoomsBySection(sectionId);
        return ResponseEntity.ok(classRoomService.getClassRoomsBySection(sectionId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalClassRooms() {
        // Long total = classRoomService.getTotalClassRooms();
        return ResponseEntity.ok(classRoomService.getTotalClassRooms());
    }
}

package com.school.platform.staff.web;

import com.school.platform.staff.application.dto.position.*;
import com.school.platform.staff.application.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<PositionFullDTO> create(@RequestBody PositionFullDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionFullDTO> update(@PathVariable Long id, @RequestBody PositionFullDTO dto) {
        return ResponseEntity.ok(positionService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(positionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PositionFullDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(positionService.findAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

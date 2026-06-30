package com.school.platform.identityaccess.web;

import com.school.platform.identityaccess.application.dto.person.*;
import com.school.platform.identityaccess.application.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonFullDTO> create(@RequestBody PersonFullDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonFullDTO> update(@PathVariable Long id, @RequestBody PersonFullDTO dto) {
        return ResponseEntity.ok(personService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PersonMediumDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(personService.findAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

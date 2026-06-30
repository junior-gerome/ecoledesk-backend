package com.school.platform.staff.web;

import com.school.platform.staff.application.dto.employee.*;
import com.school.platform.staff.application.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeFullDTO> create(
            @RequestParam Long personId,
            @RequestParam String employeeNumber,
            @RequestParam LocalDate hireDate) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(employeeService.create(personId, employeeNumber, hireDate));
    }

    @PostMapping("/{id}/positions")
    public ResponseEntity<Void> assignPosition(
            @PathVariable Long id,
            @RequestParam Long positionId,
            @RequestParam LocalDate startDate) {
        employeeService.assignPosition(id, positionId, startDate);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/positions/{id}/end")
    public ResponseEntity<Void> endPosition(
            @PathVariable Long id,
            @RequestParam LocalDate endDate) {
        employeeService.endPosition(id, endDate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeMediumDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(employeeService.findAll(pageable));
    }
}

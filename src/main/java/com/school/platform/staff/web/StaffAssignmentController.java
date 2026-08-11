package com.school.platform.staff.web;

import com.school.platform.staff.application.dto.StaffAssignmentCreateRequest;
import com.school.platform.staff.application.dto.StaffAssignmentFullDTO;
import com.school.platform.staff.application.dto.StaffAssignmentMediumDTO;
import com.school.platform.staff.application.dto.position.StaffPositionDTO;
import com.school.platform.staff.application.interfaces.StaffAssignmentService;
import com.school.platform.staff.application.interfaces.StaffPositionService;
import com.school.platform.staff.domain.model.StaffPosition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/assignments")
@RequiredArgsConstructor
public class StaffAssignmentController {

    private final StaffAssignmentService staffAssignmentService;
    private final StaffPositionService staffPositionService;

    @GetMapping("/member/{staffMemberId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<StaffAssignmentMediumDTO>> getByMember(@PathVariable Long staffMemberId) {
        return ResponseEntity.ok(staffAssignmentService.getByStaffMember(staffMemberId));
    }

    @GetMapping("/member/{staffMemberId}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<StaffAssignmentMediumDTO>> getActiveByMember(@PathVariable Long staffMemberId) {
        return ResponseEntity.ok(staffAssignmentService.getActiveByStaffMember(staffMemberId));
    }

    @GetMapping("/position/{position}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<StaffAssignmentMediumDTO>> getByPosition(@PathVariable StaffPosition position) {
        return ResponseEntity.ok(staffAssignmentService.getByPosition(position));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StaffAssignmentFullDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(staffAssignmentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StaffAssignmentFullDTO> create(@Valid @RequestBody StaffAssignmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffAssignmentService.create(request));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StaffAssignmentFullDTO> close(@PathVariable Long id) {
        return ResponseEntity.ok(staffAssignmentService.closeAssignment(id));
    }

    @GetMapping("/positions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<StaffPositionDTO>> getPositions() {
        return ResponseEntity.ok(staffPositionService.getAllPositions());
    }
}

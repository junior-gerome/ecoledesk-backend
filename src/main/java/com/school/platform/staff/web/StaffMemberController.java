package com.school.platform.staff.web;

import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import com.school.platform.staff.application.dto.StaffMemberCreateRequest;
import com.school.platform.staff.application.dto.StaffMemberFullDTO;
import com.school.platform.staff.application.dto.StaffMemberMediumDTO;
import com.school.platform.staff.application.interfaces.StaffMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/members")
@RequiredArgsConstructor
public class StaffMemberController {

    private final StaffMemberService staffMemberService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<StaffMemberMediumDTO>> getAll() {
        return ResponseEntity.ok(staffMemberService.getAllMedium());
    }

    @GetMapping("/basic")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<StaffMemberBasicDTO>> getAllBasic() {
        return ResponseEntity.ok(staffMemberService.getAllBasic());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StaffMemberFullDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(staffMemberService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StaffMemberFullDTO> create(@Valid @RequestBody StaffMemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffMemberService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<StaffMemberFullDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody StaffMemberCreateRequest request) {
        return ResponseEntity.ok(staffMemberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        staffMemberService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(staffMemberService.count());
    }
}

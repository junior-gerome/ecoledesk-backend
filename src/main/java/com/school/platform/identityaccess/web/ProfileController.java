package com.school.platform.identityaccess.web;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.identityaccess.application.dto.UserAccountSummaryDTO;
import com.school.platform.identityaccess.application.dto.role.RoleFullDTO;
import com.school.platform.identityaccess.application.RoleService;
import com.school.platform.identityaccess.application.UserAccountManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProfileController {

    private final RoleService roleService;
    private final UserAccountManagementService usersService;

    @GetMapping
    public ResponseEntity<Page<RoleFullDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(roleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RoleFullDTO> create(@Valid @RequestBody RoleFullDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleFullDTO> update(@PathVariable Long id, @Valid @RequestBody RoleFullDTO dto) {
        return ResponseEntity.ok(roleService.update(id, dto));
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> assignPermissions(
            @PathVariable Long id,
            @RequestBody Set<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/role/{roleId}")
    public ResponseEntity<UserAccountSummaryDTO> assignUserRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(usersService.assignRole(userId, roleId));
    }

    @DeleteMapping("/users/{userId}/role")
    public ResponseEntity<UserAccountSummaryDTO> clearUserRole(@PathVariable Long userId) {
        return ResponseEntity.ok(usersService.clearDynamicRole(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

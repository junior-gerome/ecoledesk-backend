package com.school.platform.identityaccess.web;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.identityaccess.application.dto.useraccount.UserAccountFullDTO;
import com.school.platform.identityaccess.application.dto.useraccount.UserAccountMediumDTO;
import com.school.platform.identityaccess.application.UserAccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Deprecated(since = "2026-07", forRemoval = false)
public class UserAccountController {

    private final UserAccountService userAccountService;

    @PostMapping
    public ResponseEntity<UserAccountFullDTO> create(
            @RequestParam Long personId,
            @RequestParam String username,
            @RequestParam String password) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userAccountService.create(personId, username, password));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable Long id,
            @RequestBody Set<Long> roleIds) {
        userAccountService.assignRoles(id, roleIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userAccountService.changePassword(id, newPassword);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle-enabled")
    public ResponseEntity<Void> toggleEnabled(@PathVariable Long id) {
        userAccountService.toggleEnabled(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAccountFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userAccountService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserAccountMediumDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(userAccountService.findAll(pageable));
    }
}
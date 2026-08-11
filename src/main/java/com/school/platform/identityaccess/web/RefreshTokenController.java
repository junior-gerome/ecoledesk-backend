package com.school.platform.identityaccess.web;

import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenFullDTO;
import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenMediumDTO;
import com.school.platform.identityaccess.application.interfaces.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refresh-tokens")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RefreshTokenController {

    private final IRefreshTokenService refreshTokenService;

    @GetMapping
    public ResponseEntity<Page<RefreshTokenMediumDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(refreshTokenService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefreshTokenFullDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(refreshTokenService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        refreshTokenService.revoke(id);
        return ResponseEntity.noContent().build();
    }
}

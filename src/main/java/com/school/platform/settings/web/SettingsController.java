package com.school.platform.settings.web;

import com.school.platform.settings.application.dto.AppPreferencesDTO;
import com.school.platform.settings.application.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @GetMapping("/preferences")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public AppPreferencesDTO getPreferences() {
        return settingsService.getPreferences();
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasRole('ADMIN')")
    public AppPreferencesDTO updatePreferences(@Valid @RequestBody AppPreferencesDTO payload) {
        return settingsService.updatePreferences(payload);
    }
}

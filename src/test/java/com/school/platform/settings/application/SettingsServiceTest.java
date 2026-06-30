package com.school.platform.settings.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.platform.settings.application.SettingsService;
import static org.mockito.Mockito.verify;

import com.school.platform.settings.application.SettingsService;
import static org.mockito.Mockito.when;


import com.school.platform.settings.application.SettingsService;
import com.school.platform.settings.application.dto.AppPreferencesDTO;
import com.school.platform.settings.domain.model.AppPreferences;
import com.school.platform.settings.infrastructure.persistence.AppPreferencesRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {
    @Mock
    private AppPreferencesRepository repository;

    @InjectMocks
    private SettingsService service;

    @Test
    void updatePreferencesReturnsPersistedSnapshot() {
        AppPreferencesDTO payload = AppPreferencesDTO.defaults();
        AppPreferences saved = AppPreferences.from(payload);
        when(repository.findById(AppPreferences.SINGLETON_ID)).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any(AppPreferences.class))).thenReturn(saved);

        AppPreferencesDTO updated = service.updatePreferences(payload);

        assertThat(updated).isEqualTo(payload);
        verify(repository).save(org.mockito.ArgumentMatchers.any(AppPreferences.class));
    }

    @Test
    void getPreferencesFallsBackToDefaultsWhenMissing() {
        when(repository.findById(AppPreferences.SINGLETON_ID)).thenReturn(Optional.empty());

        assertThat(service.getPreferences()).isEqualTo(AppPreferencesDTO.defaults());
    }
}

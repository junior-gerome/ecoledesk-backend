package com.school.platform.settings.application;

import com.school.platform.settings.application.dto.AppPreferencesDTO;
import com.school.platform.settings.domain.model.AppPreferences;
import com.school.platform.settings.infrastructure.persistence.AppPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class SettingsService {
    private final AppPreferencesRepository repository;

    @Transactional(readOnly = true)
    public AppPreferencesDTO getPreferences() {
        return repository.findById(AppPreferences.SINGLETON_ID)
                .map(AppPreferences::toDto)
                .orElseGet(AppPreferencesDTO::defaults);
    }

    @Transactional
    public AppPreferencesDTO updatePreferences(AppPreferencesDTO payload) {
        AppPreferences preferences = repository.findById(AppPreferences.SINGLETON_ID)
                .orElseGet(() -> AppPreferences.from(AppPreferencesDTO.defaults()));
        preferences.setId(AppPreferences.SINGLETON_ID);
        preferences.apply(payload);
        return repository.save(preferences).toDto();
    }
}

package com.school.platform.settings.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.platform.settings.domain.model.AppPreferences;

public interface AppPreferencesRepository extends JpaRepository<AppPreferences, Long> {
}

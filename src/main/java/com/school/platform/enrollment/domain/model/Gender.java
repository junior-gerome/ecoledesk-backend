package com.school.platform.enrollment.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MASCULIN("MASCULIN"),
    FEMININ("FEMININ");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (Gender gender : Gender.values()) {
            if (gender.name().equals(normalized) || gender.value.equalsIgnoreCase(normalized)) {
                return gender;
            }
        }
        if ("M".equals(normalized) || "MALE".equals(normalized) || "HOMME".equals(normalized) || "GARCON".equals(normalized) || "BOY".equals(normalized)) {
            return MASCULIN;
        }
        if ("F".equals(normalized) || "FEMALE".equals(normalized) || "FEMME".equals(normalized) || "FILLE".equals(normalized) || "GIRL".equals(normalized)) {
            return FEMININ;
        }
        return null;
    }
}
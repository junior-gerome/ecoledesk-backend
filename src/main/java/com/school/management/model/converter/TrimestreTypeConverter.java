package com.school.management.model.converter;

import com.school.management.enums.TypeTrimestre;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TrimestreTypeConverter implements AttributeConverter<TypeTrimestre, String> {

    @Override
    public String convertToDatabaseColumn(TypeTrimestre attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public TypeTrimestre convertToEntityAttribute(String dbData) {
        return dbData != null ? TypeTrimestre.valueOf(dbData) : null;
    }
}

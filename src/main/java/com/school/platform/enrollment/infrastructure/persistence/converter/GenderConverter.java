package com.school.platform.enrollment.infrastructure.persistence.converter;

import com.school.platform.enrollment.domain.model.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        return Gender.fromValue(dbData);
    }
}

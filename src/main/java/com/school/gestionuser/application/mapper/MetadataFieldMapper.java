package com.school.gestionuser.application.mapper;

import com.school.gestionuser.application.dto.query.FormSchemaDto;
import com.school.gestionuser.domain.model.profile.FieldOption;
import com.school.gestionuser.domain.model.profile.MetadataField;

public class MetadataFieldMapper {
    public FormSchemaDto.FieldDto toFieldDto(MetadataField field) {
        FormSchemaDto.FieldDto dto = new FormSchemaDto.FieldDto();
        dto.setCode(field.getFieldCode());
        dto.setLabel(field.getFieldLabel());
        dto.setType(field.getFieldType());
        dto.setRequired(field.isRequired());
        dto.setOptions(field.getOptions().stream().filter(FieldOption::isActive).map(this::toOptionDto).toList());
        return dto;
    }

    private FormSchemaDto.OptionDto toOptionDto(FieldOption option) {
        FormSchemaDto.OptionDto dto = new FormSchemaDto.OptionDto();
        dto.setValue(option.getOptionValue());
        dto.setLabel(option.getOptionLabel());
        return dto;
    }
}
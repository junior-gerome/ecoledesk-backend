package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import com.school.platform.academic.domain.model.AcademicYear;

@Mapper(componentModel = "spring")
public interface AcademicYearMapper extends EntityMapper<AcademicYearDTO, AcademicYear> {

    @Mapping(target = "libelleAcademicYear", source = "libelleAcademicYear")
    AcademicYearDTO toDto(AcademicYear entity);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    AcademicYear toEntity(AcademicYearDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AcademicYearDTO dto, @MappingTarget AcademicYear entity);

    List<AcademicYearDTO> toDto(List<AcademicYear> entityList);

    List<AcademicYear> toEntity(List<AcademicYearDTO> dtoList);
}

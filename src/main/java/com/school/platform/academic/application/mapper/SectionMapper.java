package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.SectionDTO;
import com.school.platform.academic.domain.model.Section;

@Mapper(componentModel = "spring", uses = {})
public interface SectionMapper extends EntityMapper<SectionDTO, Section> {
  SectionDTO toDto(Section entity);

  Section toEntity(SectionDTO dto);

  @Mapping(target = "id", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(SectionDTO dto, @MappingTarget Section entity);

  List<SectionDTO> toDto(List<Section> entityList);

  List<Section> toEntity(List<SectionDTO> dtoList);
  
} 

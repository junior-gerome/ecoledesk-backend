package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.school.management.dto.SectionDTO;
import com.school.management.model.Section;

@Mapper(componentModel = "spring", uses = {})
public interface SectionMapper extends EntityMapper<SectionDTO, Section> {
  SectionDTO toDto(Section entity);

  Section toEntity(SectionDTO dto);

  List<SectionDTO> toDto(List<Section> entityList);

  List<Section> toEntity(List<SectionDTO> dtoList);
  
} 

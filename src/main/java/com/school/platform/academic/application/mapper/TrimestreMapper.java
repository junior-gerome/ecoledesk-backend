package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.TrimestreDTO;
import com.school.platform.academic.domain.model.Trimestre;

@Mapper(componentModel = "spring", uses=AnneeScolaireMapper.class)
public interface TrimestreMapper extends EntityMapper<TrimestreDTO, Trimestre> {
  
  TrimestreDTO toDto(Trimestre entity);

 // @Mapping(target = "anneeScolaire", ignore=true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Trimestre toEntity(TrimestreDTO dto);

  @Mapping(target ="id", ignore = true)
  @Mapping(target ="createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(TrimestreDTO dto, @MappingTarget Trimestre entity);

  List<TrimestreDTO> toDto(List<Trimestre> entityList);

  List<Trimestre> toEntity(List<TrimestreDTO> dtoList);
}
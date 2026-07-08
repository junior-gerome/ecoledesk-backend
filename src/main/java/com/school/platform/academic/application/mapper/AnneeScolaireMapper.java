package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.AnneeScolaireDTO;
import com.school.platform.academic.domain.model.AnneeScolaire;

@Mapper(componentModel = "spring")
public interface AnneeScolaireMapper extends EntityMapper<AnneeScolaireDTO,AnneeScolaire> {
  AnneeScolaireDTO toDto(AnneeScolaire entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  AnneeScolaire toEntity(AnneeScolaireDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(AnneeScolaireDTO dto, @MappingTarget AnneeScolaire entity);

  
  List<AnneeScolaireDTO> toDto(List<AnneeScolaire> entityList);

  List<AnneeScolaire> toEntity(List<AnneeScolaireDTO> dtoList);

}

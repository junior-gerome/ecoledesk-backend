package com.school.management.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.management.dto.AnneeScolaireDTO;
import com.school.management.model.AnneeScolaire;

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

package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.AnneeScolaireDTO;
import com.school.management.model.AnneeScolaire;

@Mapper(componentModel = "spring")
public interface AnneeScolaireMapper extends EntityMapper<AnneeScolaireDTO,AnneeScolaire> {
  AnneeScolaireDTO toDto(AnneeScolaire entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  AnneeScolaire toEntity(AnneeScolaireDTO dto);

  
  List<AnneeScolaireDTO> toDto(List<AnneeScolaire> entityList);

  List<AnneeScolaire> toEntity(List<AnneeScolaireDTO> dtoList);

}

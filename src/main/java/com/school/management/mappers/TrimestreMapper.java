package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.TrimestreDTO;
import com.school.management.model.Trimestre;

@Mapper(componentModel = "spring", uses=AnneeScolaireMapper.class)
public interface TrimestreMapper extends EntityMapper<TrimestreDTO, Trimestre> {
  
  TrimestreDTO toDto(Trimestre entity);

 // @Mapping(target = "anneeScolaire", ignore=true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Trimestre toEntity(TrimestreDTO dto);

  List<TrimestreDTO> toDto(List<Trimestre> entityList);

  List<Trimestre> toEntity(List<TrimestreDTO> dtoList);
}
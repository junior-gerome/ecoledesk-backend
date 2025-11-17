package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.ParentDTO;
import com.school.management.model.Parent;

@Mapper(componentModel = "spring", uses = {})
public interface ParentMapper extends EntityMapper<ParentDTO, Parent> {
  ParentDTO toDto(Parent entity);
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Parent toEntity(ParentDTO dto);

  List<ParentDTO> toDto(List<Parent> entityList);

  List<Parent> toEntity(List<ParentDTO> dtoList);


}

package com.school.management.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.management.dto.ParentDTO;
import com.school.management.model.Parent;

@Mapper(componentModel = "spring", uses = {})
public interface ParentMapper extends EntityMapper<ParentDTO, Parent> {
  ParentDTO toDto(Parent entity);
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Parent toEntity(ParentDTO dto);

    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "createdAt", ignore = true) 
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(ParentDTO dto, @MappingTarget Parent entity);

  List<ParentDTO> toDto(List<Parent> entityList);

  List<Parent> toEntity(List<ParentDTO> dtoList);


}

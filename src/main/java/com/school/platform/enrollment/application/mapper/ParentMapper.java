package com.school.platform.enrollment.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.school.platform.enrollment.application.dto.ParentDTO;
import com.school.platform.enrollment.domain.model.Parent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParentMapper extends EntityMapper<ParentDTO, Parent> {
  ParentDTO toDto(Parent entity);

  @Mapping(target = "studentParents", ignore = true)
  Parent toEntity(ParentDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "studentParents", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "active", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(ParentDTO dto, @MappingTarget Parent entity);

  List<ParentDTO> toDto(List<Parent> entityList);

  List<Parent> toEntity(List<ParentDTO> dtoList);
}
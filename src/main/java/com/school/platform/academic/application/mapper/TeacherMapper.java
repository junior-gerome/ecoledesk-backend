package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.TeacherDTO;
import com.school.platform.academic.domain.model.Teacher;

@Mapper(componentModel = "spring", uses = {})
public interface TeacherMapper extends EntityMapper<TeacherDTO, Teacher> {
  TeacherDTO toDto(Teacher entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Teacher toEntity(TeacherDTO dto);

  @Mapping(target = "id", ignore =true)
  @Mapping(target ="createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(TeacherDTO dto, @MappingTarget Teacher entity);

  List<TeacherDTO> toDto(List<Teacher> entityList);

  List<Teacher> toEntity(List<TeacherDTO> dtoList); 



}

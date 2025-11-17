package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.TeacherDTO;
import com.school.management.model.Teacher;

@Mapper(componentModel = "spring", uses = {})
public interface TeacherMapper extends EntityMapper<TeacherDTO, Teacher> {
  TeacherDTO toDto(Teacher entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Teacher toEntity(TeacherDTO dto);

  List<TeacherDTO> toDto(List<Teacher> entityList);

  List<Teacher> toEntity(List<TeacherDTO> dtoList); 



}

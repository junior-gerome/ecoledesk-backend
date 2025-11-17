package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.StudentDTO;
import com.school.management.model.*;


@Mapper(componentModel = "spring", uses = ParentMapper.class)
public interface StudentMapper extends EntityMapper<StudentDTO, Student> {
  
  StudentDTO toDto(Student entity);

  @Mapping(target = "parent", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "registrationDate", ignore = true)
  @Mapping(target = "active", ignore = true)
  //@Mapping(source = "parent", target = "parent")
  Student toEntity(StudentDTO dto);

  List<StudentDTO> toDto(List<Student> entityList);

  List<Student> toEntity(List<StudentDTO> dtoList);


  
}

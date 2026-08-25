package com.school.platform.enrollment.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.domain.model.Student;

@Mapper(componentModel = "spring", uses = GuardianMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper extends EntityMapper<StudentDTO, Student> {

  @Mapping(target = "studentNumber", source = "studentNumber")
  StudentDTO toDto(Student entity);

  @Mapping(target = "studentGuardians", ignore = true)
  @Mapping(target = "guardian", ignore = true)
  Student toEntity(StudentDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "studentGuardians", ignore = true)
  @Mapping(target = "guardian", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "active", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(StudentDTO dto, @MappingTarget Student entity);

  List<StudentDTO> toDto(List<Student> entityList);

  List<Student> toEntity(List<StudentDTO> dtoList);
}

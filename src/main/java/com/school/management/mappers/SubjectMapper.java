package com.school.management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.SubjectDTO;
import com.school.management.model.Subject;

@Mapper(componentModel = "spring", uses={})
public interface SubjectMapper {

  SubjectDTO toDto(Subject entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "actif", ignore = true)
  Subject toEntity(SubjectDTO dto);

}

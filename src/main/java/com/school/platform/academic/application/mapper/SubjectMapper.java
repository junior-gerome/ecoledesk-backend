package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.subject.SubjectDTO;
import com.school.platform.academic.domain.model.Subject;

@Mapper(componentModel = "spring", uses={})
public interface SubjectMapper extends EntityMapper<SubjectDTO,Subject> {

  SubjectDTO toDto(Subject entity);

  @Mapping(target = "actif", ignore = true)
  Subject toEntity(SubjectDTO dto);

  @Mapping(target ="id", ignore =true)
  @Mapping(target = "actif", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(SubjectDTO dto, @MappingTarget Subject entity);

  List<SubjectDTO> toDto(List<Subject> entityList);

  List<Subject> toEntity(List<SubjectDTO> dtoList);

}

package com.school.platform.academic.application.mapper;

import com.school.platform.academic.application.dto.subject.SubjectReponse;
import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.domain.model.Subject;

@Mapper(componentModel = "spring", uses={})
public interface SubjectReponseMapper extends EntityMapper<SubjectReponse,Subject> {

  SubjectReponse toDto(Subject entity);

  @Mapping(target = "active", ignore = true)
  Subject toEntity(SubjectReponse dto);

  @Mapping(target ="id", ignore =true)
  @Mapping(target = "active", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(SubjectReponse dto, @MappingTarget Subject entity);

  List<SubjectReponse> toDto(List<Subject> entityList);

  List<Subject> toEntity(List<SubjectReponse> dtoList);

}

package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.sequence.SequenceDTO;
import com.school.platform.academic.domain.model.Sequence;

@Mapper(componentModel = "spring", uses=TrimestreMapper.class)
public interface SequenceMapper extends EntityMapper<SequenceDTO, Sequence> {
  SequenceDTO toDto(Sequence entity);

  // @Mapping(target = "trimestre", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Sequence toEntity(SequenceDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(SequenceDTO dto, @MappingTarget Sequence entity);

  List<SequenceDTO> toDto(List<Sequence> entityList);

  List<Sequence> toEntity(List<SequenceDTO> dtoList);
}
package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.school.management.dto.SequenceDTO;
import com.school.management.model.Sequence;

@Mapper(componentModel = "spring", uses=TrimestreMapper.class)
public interface SequenceMapper extends EntityMapper<SequenceDTO, Sequence> {
  SequenceDTO toDto(Sequence entity);

  // @Mapping(target = "trimestre", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Sequence toEntity(SequenceDTO dto);

  List<SequenceDTO> toDto(List<Sequence> entityList);

  List<Sequence> toEntity(List<SequenceDTO> dtoList);
}
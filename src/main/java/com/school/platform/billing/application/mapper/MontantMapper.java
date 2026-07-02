package com.school.platform.billing.application.mapper;

import com.school.platform.academic.application.mapper.ClasseRoomMapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.billing.application.dto.MontantDTO;
import com.school.platform.billing.domain.model.Montant;


@Mapper(componentModel = "spring", uses={ClasseRoomMapper.class})
public interface MontantMapper extends EntityMapper<MontantDTO, Montant> {
    @Mapping(source = "classeRoom.id",target = "classeRoomId")
  MontantDTO toDto(Montant entity);
  
  @Mapping(source = "classeRoomId",target = "classeRoom.id")
  // @Mapping(target = "Montant.createdAt", ignore = true)
  // @Mapping(target = "Montant.updatedAt", ignore = true)
  Montant toEntity(MontantDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "classeRoom", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(MontantDTO dto, @MappingTarget Montant entity);

  List<MontantDTO> toDto(List<Montant> entityList);

  List<Montant> toEntity(List<MontantDTO> dtoList);


}

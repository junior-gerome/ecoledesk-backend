package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.MontantDTO;
import com.school.management.model.Montant;


@Mapper(componentModel = "spring", uses={ClasseRoomMapper.class})
public interface MontantMapper extends EntityMapper<MontantDTO, Montant> {
    @Mapping(source = "classeRoom.id",target = "classeRoomId")
  MontantDTO toDto(Montant entity);
  
  @Mapping(source = "classeRoomId",target = "classeRoom.id")
  // @Mapping(target = "Montant.createdAt", ignore = true)
  // @Mapping(target = "Montant.updatedAt", ignore = true)
  Montant toEntity(MontantDTO dto);

  List<MontantDTO> toDto(List<Montant> entityList);

  List<Montant> toEntity(List<MontantDTO> dtoList);


}

package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.school.management.dto.InscriptionStudentDTO;
import com.school.management.model.*;


@Mapper(componentModel = "spring", uses = {StudentMapper.class, AnneeScolaireMapper.class, ClasseRoomMapper.class, MontantMapper.class })
public interface InscriptionStudentMapper extends EntityMapper<InscriptionStudentDTO, InscriptionStudent> {
  @Mapping(source = "classeRoom.id", target = "classeRoomId")
  @Mapping(source = "montant.id", target = "montantId")
  @Mapping(source = "anneeScolaire.id", target = "anneeScolaireId")
  InscriptionStudentDTO toDto(InscriptionStudent entity);

  @Mapping(source = "student", target = "student", ignore = true)
  @Mapping(source = "classeRoomId", target = "classeRoom.id")
  @Mapping(source = "montantId", target = "montant.id")
  @Mapping(source = "anneeScolaireId", target = "anneeScolaire.id")
  InscriptionStudent toEntity(InscriptionStudentDTO dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(InscriptionStudentDTO dto, @MappingTarget InscriptionStudent entity);

  List<InscriptionStudentDTO> toDto(List<InscriptionStudent> entityList);
  List<InscriptionStudent> toEntity(List<InscriptionStudentDTO> dtoList);
  
} 

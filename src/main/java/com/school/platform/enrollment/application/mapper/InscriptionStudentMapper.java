package com.school.platform.enrollment.application.mapper;

import com.school.platform.billing.application.mapper.MontantMapper;

import com.school.platform.academic.application.mapper.ClasseRoomMapper;

import com.school.platform.academic.application.mapper.AnneeScolaireMapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.school.platform.enrollment.application.dto.InscriptionStudentDTO;
import com.school.platform.enrollment.domain.model.InscriptionStudent;


@Mapper(componentModel = "spring", uses = {StudentMapper.class, AnneeScolaireMapper.class, ClasseRoomMapper.class, MontantMapper.class })
public interface InscriptionStudentMapper extends EntityMapper<InscriptionStudentDTO, InscriptionStudent> {
  @Mapping(source = "classeRoom.id", target = "classeRoomId")
  @Mapping(source = "classeRoom.section.id", target = "sectionId")
  @Mapping(source = "montant.id", target = "montantId")
  @Mapping(source = "anneeScolaire.id", target = "anneeScolaireId")
  InscriptionStudentDTO toDto(InscriptionStudent entity);

  @Mapping(source = "student", target = "student", ignore = true)
  @Mapping(source = "classeRoomId", target = "classeRoom.id")
  @Mapping(source = "montantId", target = "montant.id")
  @Mapping(source = "anneeScolaireId", target = "anneeScolaire.id")
  @Mapping(target = "version", ignore = true)
  InscriptionStudent toEntity(InscriptionStudentDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "classeRoom", ignore = true)
  @Mapping(target = "montant", ignore = true)
  @Mapping(target = "anneeScolaire", ignore = true)
  @Mapping(target = "student", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(InscriptionStudentDTO dto, @MappingTarget InscriptionStudent entity);

  List<InscriptionStudentDTO> toDto(List<InscriptionStudent> entityList);
  List<InscriptionStudent> toEntity(List<InscriptionStudentDTO> dtoList);
  
} 

package com.school.management.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.management.dto.ClasseRoomDTO;
import com.school.management.model.ClasseRoom;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, TeacherMapper.class, AnneeScolaireMapper.class})
public interface ClasseRoomMapper extends EntityMapper<ClasseRoomDTO, ClasseRoom> {
  
  ClasseRoomDTO toDto(ClasseRoom entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "actif", ignore = true)
  ClasseRoom toEntity(ClasseRoomDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "actif", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(ClasseRoomDTO dto, @MappingTarget ClasseRoom entity);

  List<ClasseRoomDTO> toDto(List<ClasseRoom> entityList);

  List<ClasseRoom> toEntity(List<ClasseRoomDTO> dtoList);
  
} 
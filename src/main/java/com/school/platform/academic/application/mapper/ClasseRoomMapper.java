package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.ClasseRoomDTO;
import com.school.platform.academic.domain.model.ClasseRoom;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, TeacherMapper.class, AcademicYearMapper.class})
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
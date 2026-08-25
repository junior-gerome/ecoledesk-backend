package com.school.platform.academic.application.mapper;

import com.school.platform.shared.application.mapper.EntityMapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.classeroom.ClasseRoomDTO;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.staff.application.mapper.StaffMemberMapper;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, StaffMemberMapper.class, AcademicYearMapper.class})
public interface ClasseRoomMapper extends EntityMapper<ClasseRoomDTO, ClasseRoom> {
  
  ClasseRoomDTO toDto(ClasseRoom entity);

  @Mapping(target = "id", ignore = true)
//  @Mapping(target = "actif", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "section", ignore = true)
  @Mapping(target = "academicYear", ignore = true)
  @Mapping(target = "teacher", ignore = true)
  ClasseRoom toEntity(ClasseRoomDTO dto);

  @Mapping(target = "id", ignore = true)
//  @Mapping(target = "actif", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  @Mapping(target = "section", ignore = true)
  @Mapping(target = "academicYear", ignore = true)
  @Mapping(target = "teacher", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(ClasseRoomDTO dto, @MappingTarget ClasseRoom entity);

  List<ClasseRoomDTO> toDto(List<ClasseRoom> entityList);

  List<ClasseRoom> toEntity(List<ClasseRoomDTO> dtoList);
  
} 

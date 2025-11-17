package com.school.management.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.school.management.dto.ClasseRoomDTO;
import com.school.management.model.ClasseRoom;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, TeacherMapper.class, AnneeScolaireMapper.class})
public interface ClasseRoomMapper extends EntityMapper<ClasseRoomDTO, ClasseRoom> {
  
  // @Mapping(target = "section", ignore = true)
  // @Mapping(target = "teacher", ignore = true)
  // @Mapping(target = "anneeScolaire", ignore = true)
  ClasseRoomDTO toDto(ClasseRoom entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  // @Mapping(source = "sectionId", target = "section.id")
  // @Mapping(source = "teacherId", target = "teacher.id")
  // @Mapping(source = "anneeScolaireId", target = "anneeScolaire.id")
  @Mapping(target = "actif", ignore = true)
  ClasseRoom toEntity(ClasseRoomDTO dto);

  List<ClasseRoomDTO> toDto(List<ClasseRoom> entityList);

  List<ClasseRoom> toEntity(List<ClasseRoomDTO> dtoList);
  
} 
package com.school.management.mappers;

import com.school.management.dto.StudentDTO;
import com.school.management.model.*;

public class StudentMapper {
  // Entity → DTO
  public static StudentDTO toDTO(Student entity) {
    if (entity == null)
      return null;
    
    StudentDTO dto = new StudentDTO();
    dto.setId(entity.getId());
    dto.setFirstNameStudent(entity.getFirstNameStudent());
    dto.setLastNameStudent(entity.getLastNameStudent());
    dto.setDateOfBirth(entity.getDateOfBirth());
    dto.setGender(entity.getGender());
    dto.setActive(entity.getActive());
    dto.setEcolePrecedente(entity.getEcolePrecedente());
    if (entity.getParent() != null) {
      dto.setParentId(entity.getParent().getId());
    }
    return dto;
  }

  // DTO → Entity
  public static Student toEntity(StudentDTO dto) {
    if (dto == null) return null;

    Student entity = new Student();
    entity.setId(dto.getId());
    entity.setFirstNameStudent(dto.getFirstNameStudent());
    entity.setLastNameStudent(dto.getLastNameStudent());
    entity.setDateOfBirth(dto.getDateOfBirth());
    entity.setGender(dto.getGender());
    entity.setActive(dto.getActive());
    entity.setEcolePrecedente(dto.getEcolePrecedente());
    

    if (dto.getParentId() != null) {
      Parent parent = new Parent();
      parent.setId(dto.getParentId());
      entity.setParent(parent);
    }

    return entity;
  }

}

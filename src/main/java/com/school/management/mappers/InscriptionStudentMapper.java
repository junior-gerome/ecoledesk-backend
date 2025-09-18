package com.school.management.mappers;

import com.school.management.dto.InscriptionStudentDTO;
import com.school.management.model.*;



public class InscriptionStudentMapper {

  // Entity → DTO
  public static InscriptionStudentDTO toDTO(InscriptionStudent entity) {
    if (entity == null)
      return null;
    InscriptionStudentDTO dto = new InscriptionStudentDTO();
    dto.setId(entity.getId());
    dto.setStudentId(entity.getStudent().getId());
    dto.setClasseRoomId(entity.getClasseRoom().getId());
    dto.setMontantId(entity.getMontant().getId());
    dto.setAnneeScolaireId(entity.getAnneeScolaire().getId());
    dto.setDateInscription(entity.getDateInscription());
    return dto;
  }
  

  // DTO → Entity
  public static InscriptionStudent toEntity(InscriptionStudentDTO dto) {
    if(dto == null) return null;

    InscriptionStudent entity = new InscriptionStudent();
    entity.setId(dto.getId());
    entity.setDateInscription(dto.getDateInscription());

    Student student = new Student();
    student.setId(dto.getStudentId());
    entity.setStudent(student);

    ClasseRoom classeRoom = new ClasseRoom();
    classeRoom.setId(dto.getClasseRoomId());
    entity.setClasseRoom(classeRoom);

    Montant montant = new Montant();
    montant.setId(dto.getMontantId());
    entity.setMontant(montant);

    AnneeScolaire anneeScolaire = new AnneeScolaire();
    anneeScolaire.setId(dto.getAnneeScolaireId());
    entity.setAnneeScolaire(anneeScolaire);

    return entity;
  }

}

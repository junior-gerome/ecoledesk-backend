package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.ClasseRoomStudentCountDTO;
import com.school.management.dto.InscriptionStudentDTO;
import com.school.management.dto.SectionStudentCountDTO;
import com.school.management.dto.StudentDTO;
import com.school.management.mappers.InscriptionStudentMapper;
import com.school.management.model.ClasseRoom;
import com.school.management.model.InscriptionStudent;
import com.school.management.model.Student;
import com.school.management.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InscriptionStudentService {
  
  private final InscriptionStudentRepository inscriptionStudentRepository;
  private final StudentRepository studentRepository;
  private final ClasseRoomRepository classeRoomRepository;
  private final SectionRepository sectionRepository;
  private final StudentService studentService;
  private final InscriptionStudentMapper mapper;

  public InscriptionStudentDTO createInscription(InscriptionStudentDTO dto) {
    
    StudentDTO savedStudentDTO = studentService.createStudentWithParent(dto.getStudent());
  
  
    Student studentEntity = studentRepository.findById(savedStudentDTO.getId())
        .orElseThrow(() -> new IllegalStateException("Student not found after creation"));
    
    
    InscriptionStudent entity = mapper.toEntity(dto);
    
  
    entity.setStudent(studentEntity);
    
    
    InscriptionStudent saved = inscriptionStudentRepository.save(entity);
    
    return mapper.toDto(saved);
  }

  
  public InscriptionStudentDTO updateInscription(Long id, InscriptionStudentDTO dto) {
    InscriptionStudent existinginscriptionStudent = inscriptionStudentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvé avec l'ID : " + id));

    InscriptionStudent inscripton = mapper.toEntity(dto);
        inscripton.setId(existinginscriptionStudent.getId());
        inscripton.setDateInscription(existinginscriptionStudent.getDateInscription());
        InscriptionStudent updated = inscriptionStudentRepository.save(inscripton);
        return mapper.toDto(updated);
  }

  public List<InscriptionStudentDTO> getAll() {
    return inscriptionStudentRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
  }

  public InscriptionStudentDTO getById(Long id) {
    return inscriptionStudentRepository.findById(id).map(mapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée avec l'ID : " + id));
  }

  public InscriptionStudentDTO getByStudentAndClass(Long studentId, Long classeRoomId) {
    return inscriptionStudentRepository.findByStudentIdAndClasseRoomId(studentId, classeRoomId).map(mapper::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée pour l'étudiant avec l'ID : "
            + studentId + " et la classe avec l'ID : " + classeRoomId));
  }
  
  public long getCountByClass(Long classeRoomId) {
    return inscriptionStudentRepository.countByClasseRoomId(classeRoomId);
        //.orElseThrow(() -> new ResourceNotFoundException("Le Nombre d'etudiant pas section non trouver:" + sectionId + "et la classe avec l'ID:" + classId));
  }

  // Supprimer inscription
  public void delete(Long id) {
    if (!inscriptionStudentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Inscription non trouvée avec l'ID : " + id);
    }
    inscriptionStudentRepository.deleteById(id);
  }

  public List<SectionStudentCountDTO> getStudentCountBySection() {

    return sectionRepository.findAll().stream()
        .map(section -> {
          // Récupérer toutes les classes de la section
          List<ClasseRoom> classes = classeRoomRepository.findBySection(section);

          // Compter le total des élèves pour ces classes
          long totalStudents = classes.stream()
              .mapToLong(classe -> inscriptionStudentRepository.countByClasseRoom(classe))
              .sum();

          // Retourner le DTO
          return new SectionStudentCountDTO(section.getLibelle(), totalStudents);
        })
        .collect(Collectors.toList());
  }
    
    public List<ClasseRoomStudentCountDTO> getStudentCountByClasse() {
    return classeRoomRepository.findAll().stream()
        .map(classe -> {
            long totalStudents = inscriptionStudentRepository.countByClasseRoomId(classe.getId());
            return new ClasseRoomStudentCountDTO(classe.getNameClasse(), totalStudents);
        })
        .collect(Collectors.toList());
}



}

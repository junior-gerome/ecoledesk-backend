package com.school.management.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.InscriptionStudentDTO;
import com.school.management.dto.StudentDTO;
import com.school.management.mappers.InscriptionStudentMapper;
import com.school.management.mappers.StudentMapper;
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
  private final AnneeScolaireRepository anneeScolaireRepository;
  private final ClasseRoomRepository classeRoomRepository;
  private final MontantRepository montantRepository;
  private final ParentRepository parentRepository;

  // Méthode création inscription avec StudentDTO
  public InscriptionStudentDTO createInscription(StudentDTO studentDTO, Long classeRoomId, Long montantId, Long anneeScolaireId) {
    
    // 1. Convertir StudentDTO → Student et sauvegarder
    Student student = StudentMapper.toEntity(studentDTO);
    Student savedStudent = studentRepository.save(student);

    // 2. Construire InscriptionStudentDTO
    InscriptionStudentDTO dto = new InscriptionStudentDTO();
    dto.setStudentId(savedStudent.getId());
    dto.setClasseRoomId(classeRoomId);
    dto.setMontantId(montantId);
    dto.setAnneeScolaireId(anneeScolaireId);
    dto.setDateInscription(LocalDate.now());

    // 3. Mapper DTO → Entity
    InscriptionStudent inscription = InscriptionStudentMapper.toEntity(dto);

    // ⚠ Charger les vraies entités (sinon tu as juste des IDs "fantômes")
    inscription.setStudent(savedStudent);
    inscription.setClasseRoom(classeRoomRepository.findById(classeRoomId)
        .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée avec l'ID : " + classeRoomId)));
    inscription.setMontant(montantRepository.findById(montantId)
        .orElseThrow(() -> new ResourceNotFoundException("Montant non trouvé avec l'ID : " + montantId)));
    inscription.setAnneeScolaire(anneeScolaireRepository.findById(anneeScolaireId)
        .orElseThrow(() -> new ResourceNotFoundException("Année scolaire non trouvée avec l'ID : " + anneeScolaireId)));

    // 4. Sauvegarder inscription
    InscriptionStudent saved = inscriptionStudentRepository.save(inscription);

    // 5. Retourner DTO
    return InscriptionStudentMapper.toDTO(saved);
  }

  // Récupérer toutes les inscriptions
  public List<InscriptionStudent> getAll() {
    return inscriptionStudentRepository.findAll();
  }

  // Récupérer inscription par ID
  public InscriptionStudent getById(Long id) {
    return inscriptionStudentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée avec l'ID : " + id));
  }

  public InscriptionStudent getByStudentAndClass(Long studentId, Long classeRoomId) {
    return inscriptionStudentRepository.findByStudentIdAndClasseRoomId(studentId, classeRoomId)
        .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvée pour l'étudiant avec l'ID : "
            + studentId + " et la classe avec l'ID : " + classeRoomId));
  }
  
  public long getCountByClass(Long classeRoomId) {
    return inscriptionStudentRepository.countByClasseRoomId(classeRoomId);
        // .orElseThrow(() -> new ResourceNotFoundException("Le Nombre d'etudiant pas section non trouver:" + sectionId + "et la classe avec l'ID:" + classId));
  }

  // Supprimer inscription
  public void delete(Long id) {
    if (!inscriptionStudentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Inscription non trouvée avec l'ID : " + id);
    }
    inscriptionStudentRepository.deleteById(id);
  }
}

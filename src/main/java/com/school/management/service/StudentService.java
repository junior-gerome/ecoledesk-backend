package com.school.management.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.ParentDTO;
import com.school.management.dto.StudentDTO;
import com.school.management.dto.StudentReportDTO;
import com.school.management.mappers.ParentMapper;
import com.school.management.mappers.StudentMapper;
import com.school.management.model.Parent;
import com.school.management.model.Student;
import com.school.management.repository.ParentRepository;
import com.school.management.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentMapper studentMapper;
    private final ParentMapper parentMapper;

    @Transactional
public StudentDTO createStudentWithParent(StudentDTO studentDTO) {
    if (studentDTO == null || studentDTO.getParent() == null) {
        throw new IllegalArgumentException("Les informations de l'élève et du parent sont requises.");
    }

    ParentDTO parentDto = studentDTO.getParent();
    Parent parentEntity;

    Optional<Parent> existingParent = Optional.empty();


    if (parentDto.getEmail() != null && !parentDto.getEmail().isBlank()) {
        existingParent = parentRepository.findByEmail(parentDto.getEmail());
    }

    if (existingParent.isEmpty()
            && parentDto.getPhoneNumber() != null
            && !parentDto.getPhoneNumber().isBlank()) {
        existingParent = parentRepository.findByPhoneNumber(parentDto.getPhoneNumber());
    }

    
    if (existingParent.isPresent()) {
        parentEntity = existingParent.get();
        log.info("Parent existant trouvé : {}", parentEntity.getEmail());
    } else {
        parentEntity = parentMapper.toEntity(parentDto);
        parentEntity = parentRepository.save(parentEntity); 
        parentRepository.flush();
        log.info("Nouveau parent créé : {}", parentEntity.getEmail());
    }

    Student studentEntity = studentMapper.toEntity(studentDTO);
    studentEntity.setParent(parentEntity);                   
    studentEntity.setRegistrationDate(LocalDateTime.now());
    studentEntity.setActive(true);

    Student savedStudent = studentRepository.save(studentEntity);
    log.info("Élève enregistré : {} {} (ID: {})", savedStudent.getFirstNameStudent(),
             savedStudent.getLastNameStudent(), savedStudent.getId());

    return studentMapper.toDto(savedStudent);
}


    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByParentId(Long parentId, Pageable pageable) {
        return studentRepository.findByParentId(parentId, pageable)
                .map(studentMapper::toDto);
    }

    // 🔹 Liste de tous les étudiants
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toDto)
                .collect(Collectors.toList());
    }

    // 🔹 Récupérer un étudiant par son ID
    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(studentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id));
    }

  
    // 🔹 Mettre à jour un étudiant
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id));

        studentMapper.updateEntityFromDto(dto, existingStudent);
        // Student entity = studentMapper.toEntity(dto);
        // entity.setId(existingStudent.getId());
        // entity.setRegistrationDate(existingStudent.getRegistrationDate());

        if (dto.getParent() != null && dto.getParent().getId()!=null) {
           Parent parent = parentRepository.findById(dto.getParent().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent non trouvé avec l'ID : " + dto.getParent().getId()));
        existingStudent.setParent(parent);
        }

        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("Étudiant mis à jour : {}", updatedStudent.getId());

        return studentMapper.toDto(updatedStudent);
    }

    // 🔹 Supprimer un étudiant
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id);
        }
        studentRepository.deleteById(id);
        log.info("Étudiant supprimé avec ID: {}", id);
    }

    // 🔹 Génération de rapport (à implémenter)
    public StudentReportDTO generateStudentReport(String studentId, String period) {
        // TODO: Implémenter la génération de rapport
        return new StudentReportDTO();
    }

    // 🔹 Étudiant courant (ex: depuis le contexte utilisateur)
    @Transactional(readOnly = true)
    public Optional<StudentDTO> getCurrentStudent() {
        // Implémentation réelle basée sur le contexte de sécurité
        return Optional.empty();
    }

    // 🔹 Compter le nombre total d’élèves
    @Transactional(readOnly = true)
    public long getTotalStudents() {
        return studentRepository.count();
    }
}

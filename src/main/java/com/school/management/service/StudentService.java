package com.school.management.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.StudentDTO;
import com.school.management.dto.StudentReportDTO;
import com.school.management.model.Student;
import com.school.management.repository.ClasseRepository;
import com.school.management.repository.SectionRepository;
import com.school.management.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final ClasseRepository classeRepository;

    

    // 1. Recherche d'étudiants avec pagination
    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByClasse(Long classeId, Pageable pageable) {
        return studentRepository.findByClasseId(classeId, pageable) 
                .map(this::convertToStudentDTO);
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByParent(Long parentId, Pageable pageable) {
        return studentRepository.findByParentId(parentId, pageable)
                .map(this::convertToStudentDTO);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsBySection(Long sectionId, Pageable pageable) {
        return studentRepository.findBySectionId(sectionId, pageable)
                .map(this::convertToStudentDTO);
    }

    // 2. Opérations CRUD optimisées
    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(this::convertToStudentDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id));
    }

    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = new Student();
        mapDtoToEntity(studentDTO, student);
        
        Student savedStudent = studentRepository.save(student);
        log.info("Étudiant créé avec ID: {}", savedStudent.getId());
        return convertToStudentDTO(savedStudent);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id));
        
        mapDtoToEntity(studentDTO, student);
        
        Student updatedStudent = studentRepository.save(student);
        log.info("Étudiant mis à jour avec ID: {}", id);
        return convertToStudentDTO(updatedStudent);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id);
        }
        studentRepository.deleteById(id);
        log.info("Étudiant supprimé avec ID: {}", id);
    }

    // Méthode de conversion optimisée d'un Student en StudentDTO
private StudentDTO convertToStudentDTO(Student student) {
    return StudentDTO.builder()
            .id(student.getId())
            .lastNameStudent(student.getLastNameStudent())
            .firstNameStudent(student.getFirstNameStudent())
            .dateOfBirth(student.getDateOfBirth())
            .registrationDate(student.getRegistrationDate())
            .gender(student.getGender()) // Conversion enum -> String
            .ecolePrecedente(student.getEcolePrecedente())
            // Mappage des relations avec gestion des nulls
            .classe(student.getClasse())
            .Section(student.getSection() )
            .build();
}

    private void mapDtoToEntity(StudentDTO dto, Student entity) {
        entity.setLastNameStudent(dto.getLastNameStudent());
        entity.setFirstNameStudent(dto.getFirstNameStudent());
        entity.setGender(dto.getGender());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setRegistrationDate(dto.getRegistrationDate() != null 
                ? dto.getRegistrationDate()
                : LocalDate.now().atStartOfDay());
        entity.setEcolePrecedente(dto.getEcolePrecedente());
        // Gestion des relations avec les entités
        if (dto.getClasse() != null) {
            entity.setClasse(classeRepository.findById(dto.getClasse().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classe non trouvée: " + dto.getClasse().getId())));
        }
        if (dto.getSection() != null) {
            entity.setSection(sectionRepository.findById(dto.getSection().getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Section non trouvée: " + dto.getSection().getId())));
        }
    }

    public StudentReportDTO generateStudentReport(String studentId, String period) {
        // TODO: Implement the logic to generate student report based on studentId and period
        return new StudentReportDTO();
    }
    
    // 4. Statistiques et méthodes utilitaires
    @Transactional(readOnly = true)
    public long countStudentsInClass(Long classeId) {
        return studentRepository.countByClasseId(classeId);
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getCurrentStudent() {
        // Implémentation réelle basée sur le contexte de sécurité
        // Exemple simplifié :
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // String username = auth.getName();
        // return studentRepository.findByEmail(username).map(this::convertToStudentDTO);
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public int getAbsenceDays(Long student_Id) {
        // Implémentation réelle à compléter
        return studentRepository.countAbsencesByStudentId(student_Id);
    }
}
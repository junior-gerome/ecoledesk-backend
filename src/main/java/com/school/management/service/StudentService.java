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
import com.school.management.dto.StudentDTO;
import com.school.management.dto.StudentReportDTO;
import com.school.management.mappers.StudentMapper;
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

    @Transactional(readOnly = true)
    public Page<StudentDTO> getStudentsByParentId(Long parentId, Pageable pageable) {
        return studentRepository.findByParentId(parentId, pageable)
                .map(StudentMapper::toDTO);
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(StudentMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id));
    }

    public StudentDTO createStudent(StudentDTO dto) {
        Student entity = StudentMapper.toEntity(dto);

        // Charge le parent si l'ID est présent
        if (dto.getParentId() != null) {
            entity.setParent(parentRepository.findById(dto.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent non trouvé avec l'ID : " + dto.getParentId())));
        }

        // ⚡ Générer automatiquement la date d'inscription si elle est nulle
        if (entity.getRegistrationDate() == null) {
            entity.setRegistrationDate(LocalDateTime.now());
        }

        Student savedStudent = studentRepository.save(entity);
        log.info("Étudiant créé avec ID: {}", savedStudent.getId());
        return StudentMapper.toDTO(savedStudent);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id));

        // Conserver la date d'inscription existante
        Student entity = StudentMapper.toEntity(dto);
        entity.setId(existingStudent.getId());
        entity.setRegistrationDate(existingStudent.getRegistrationDate());

        // Charge le parent si l'ID est présent
        if (dto.getParentId() != null) {
            entity.setParent(parentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent non trouvé avec l'ID : " + dto.getParentId())));
        }

        Student updatedStudent = studentRepository.save(entity);
        log.info("Étudiant mis à jour avec ID: {}", id);
        return StudentMapper.toDTO(updatedStudent);
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Étudiant non trouvé avec l'ID : " + id);
        }
        studentRepository.deleteById(id);
        log.info("Étudiant supprimé avec ID: {}", id);
    }
    
    public StudentReportDTO generateStudentReport(String studentId, String period) {
        // TODO: Implémenter la génération de rapport
        return new StudentReportDTO();
    }
    
    @Transactional(readOnly = true)
    public Optional<StudentDTO> getCurrentStudent() {
        // Implémentation réelle basée sur le contexte de sécurité
        return Optional.empty();
    }

    public long getTotalStudents() {
        return studentRepository.count();
    }
}

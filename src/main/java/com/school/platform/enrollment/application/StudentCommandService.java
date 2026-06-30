package com.school.platform.enrollment.application;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.enrollment.application.dto.ParentDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.mapper.ParentMapper;
import com.school.platform.enrollment.application.mapper.StudentMapper;
import com.school.platform.enrollment.domain.model.Parent;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.ParentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentCommandService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final StudentMapper studentMapper;
    private final ParentMapper parentMapper;

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public StudentDTO createStudentWithParent(StudentDTO studentDTO) {
        if (studentDTO == null || studentDTO.getParent() == null) {
            throw new IllegalArgumentException("Les informations de l'eleve et du parent sont requises.");
        }

        Parent parentEntity = resolveParentForCreate(studentDTO.getParent());
        Student studentEntity = studentMapper.toEntity(studentDTO);
        studentEntity.setParent(parentEntity);
        studentEntity.setRegistrationDate(LocalDateTime.now());
        studentEntity.setActive(true);

        Student savedStudent = studentRepository.save(studentEntity);
        log.info("Eleve enregistre : {} {} (ID: {})", savedStudent.getFirstNameStudent(),
                savedStudent.getLastNameStudent(), savedStudent.getId());
        return studentMapper.toDto(savedStudent);
    }

    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public StudentDTO updateStudent(Long id, StudentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les informations de l'eleve sont requises.");
        }

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));

        studentMapper.updateEntityFromDto(dto, existingStudent);

        if (dto.getParent() != null) {
            Parent resolvedParent = resolveParentForUpdate(existingStudent, dto.getParent());
            existingStudent.setParent(resolvedParent);
        }

        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("Etudiant mis a jour : {}", updatedStudent.getId());
        return studentMapper.toDto(updatedStudent);
    }

    @Transactional
    @CacheEvict(value = "students", key = "#id")
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + id));
        student.setActive(false);
        studentRepository.save(student);
        log.info("Etudiant archive avec ID: {}", id);
    }

    @Transactional
    @CacheEvict(value = "students", key = "#studentId")
    public StudentDTO linkParent(Long studentId, Long parentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant non trouve avec l'ID : " + studentId));
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent non trouve avec l'ID : " + parentId));
        student.setParent(parent);
        return studentMapper.toDto(studentRepository.save(student));
    }

    private Parent resolveParentForCreate(ParentDTO parentDto) {
        Optional<Parent> existingParent = findExistingParentByEmailOrPhone(parentDto);
        if (existingParent.isPresent()) {
            Parent parentEntity = existingParent.get();
            log.info("Parent existant trouve : {}", parentEntity.getEmail());
            return parentEntity;
        }

        Parent parentEntity = parentMapper.toEntity(parentDto);
        Parent savedParent = parentRepository.save(parentEntity);
        parentRepository.flush();
        log.info("Nouveau parent cree : {}", savedParent.getEmail());
        return savedParent;
    }

    private Parent resolveParentForUpdate(Student existingStudent, ParentDTO parentDto) {
        Parent parentEntity = null;

        if (parentDto.getId() != null) {
            parentEntity = parentRepository.findById(parentDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent non trouve avec l'ID : " + parentDto.getId()));
        }

        if (parentEntity == null) {
            parentEntity = findExistingParentByEmailOrPhone(parentDto)
                    .orElse(existingStudent.getParent());
        }

        if (parentEntity == null) {
            Parent createdParent = parentRepository.save(parentMapper.toEntity(parentDto));
            log.info("Nouveau parent cree pendant la mise a jour : {}", createdParent.getId());
            return createdParent;
        }

        validateParentContactUniqueness(parentDto, parentEntity.getId());
        parentMapper.updateEntityFromDto(parentDto, parentEntity);
        return parentRepository.save(parentEntity);
    }

    private Optional<Parent> findExistingParentByEmailOrPhone(ParentDTO parentDto) {
        if (parentDto == null) {
            return Optional.empty();
        }

        if (hasText(parentDto.getEmail())) {
            Optional<Parent> existingByEmail = parentRepository.findByEmail(parentDto.getEmail().trim());
            if (existingByEmail.isPresent()) {
                return existingByEmail;
            }
        }

        if (hasText(parentDto.getPhoneNumber())) {
            return parentRepository.findByPhoneNumber(parentDto.getPhoneNumber().trim());
        }

        return Optional.empty();
    }

    private void validateParentContactUniqueness(ParentDTO parentDto, Long currentParentId) {
        if (parentDto == null) {
            return;
        }

        if (hasText(parentDto.getEmail())) {
            parentRepository.findByEmail(parentDto.getEmail().trim())
                    .filter(existing -> !existing.getId().equals(currentParentId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Un autre parent utilise deja cet email.");
                    });
        }

        if (hasText(parentDto.getPhoneNumber())) {
            parentRepository.findByPhoneNumber(parentDto.getPhoneNumber().trim())
                    .filter(existing -> !existing.getId().equals(currentParentId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Un autre parent utilise deja ce numero de telephone.");
                    });
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

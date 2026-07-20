package com.school.platform.academic.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.academic.application.dto.TeacherDTO;
import com.school.platform.academic.application.mapper.TeacherMapper;
import com.school.platform.academic.domain.model.Teacher;
import com.school.platform.academic.infrastructure.persistence.TeacherRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper mapper;

    @Transactional(readOnly = true)
    public List<TeacherDTO> getAllEnseignants() {
        return teacherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherDTO getEnseignantById(Long id) {
        return teacherRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
    }

    @Transactional
    public TeacherDTO createEnseignant(TeacherDTO dto) {
        // Vérification des doublons
        if (teacherRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        if (teacherRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Numéro de téléphone déjà utilisé");
        }

        Teacher teacher = new Teacher();
        updateTeacherFromDTO(teacher, dto);
        
        try {
            return convertToDTO(teacherRepository.save(teacher));
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Erreur de contrainte d'intégrité: " + e.getRootCause().getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création: " + e.getMessage());
        }
    }

    @Transactional
    public TeacherDTO updateEnseignant(Long id, TeacherDTO dto) {
        Teacher existingteacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

        mapper.updateEntityFromDto(dto,existingteacher);

        if (!existingteacher.getEmail().equals(dto.getEmail()) &&
        teacherRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Teacher updated = teacherRepository.save(existingteacher);
        log.info("Teacher mis a jour avec succes:{}", updated.getId());
        
        return mapper.toDto(updated);
    }

    @Transactional
    public void deleteEnseignant(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new RuntimeException("Enseignant non trouvé");
        }
        teacherRepository.deleteById(id);
    }

    private void updateTeacherFromDTO(Teacher teacher, TeacherDTO dto) {
        teacher.setLastnameTeacher(dto.getLastnameTeacher());
        teacher.setFirstnameTeacher(dto.getFirstnameTeacher());
        teacher.setEmail(dto.getEmail());
        teacher.setGender(dto.getGender()); 
        teacher.setPhoneNumber(dto.getPhoneNumber());
        teacher.setSpeciality(dto.getSpeciality());
        teacher.setNiveau(dto.getNiveau());
        teacher.setAdress(dto.getAdress());
        teacher.setDateEmbauche(dto.getDateEmbauche());
        teacher.setPhotoUrl(dto.getPhotoUrl());
    }

    private TeacherDTO convertToDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setLastnameTeacher(teacher.getLastnameTeacher());
        dto.setFirstnameTeacher(teacher.getFirstnameTeacher());
        dto.setEmail(teacher.getEmail());
        dto.setGender(teacher.getGender());
        dto.setNiveau(teacher.getNiveau());
        dto.setAdress(teacher.getAdress());
        dto.setDateEmbauche(teacher.getDateEmbauche());
        dto.setPhoneNumber(teacher.getPhoneNumber());
        dto.setSpeciality(teacher.getSpeciality());
        dto.setPhotoUrl(teacher.getPhotoUrl());
        return dto;
    }

    public long getTotalTeachers() {
        return teacherRepository.count();
    }
}

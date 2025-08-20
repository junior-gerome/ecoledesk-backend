package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.management.dto.TeacherDTO;
import com.school.management.model.Teacher;
import com.school.management.model.Section;
import com.school.management.repository.TeacherRepository;
import com.school.management.repository.SectionRepository; // Ajout de l'import manquant

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final AffectationService affectationService;
    private final TeacherRepository teacherRepository;

    @Autowired
    private SectionRepository sectionRepository;

    // public TeacherService(AffectationService affectationService, TeacherRepository teacherRepository) {
    //     this.affectationService = affectationService;
    //     this.teacherRepository = teacherRepository;
    // } // Ajout de l'injection de dépendance

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
        if (teacherRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Teacher teacher = new Teacher();
        updateTeacherFromDTO(teacher, dto);
        return convertToDTO(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherDTO updateEnseignant(Long id, TeacherDTO dto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));

        if (!teacher.getEmail().equals(dto.getEmail()) &&
        teacherRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        updateTeacherFromDTO(teacher, dto);
        return convertToDTO(teacherRepository.save(teacher));
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
        teacher.setFirstnameTeacher(dto.getLastnameTeacher());
        teacher.setEmail(dto.getEmail());

        Section section = sectionRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Section non trouvée"));
                teacher.setSection(section);
    }

    private TeacherDTO convertToDTO(Teacher teacher) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(teacher.getId());
        dto.setLastnameTeacher(teacher.getLastnameTeacher());
        dto.setFirstnameTeacher(teacher.getFirstnameTeacher());
        dto.setEmail(teacher.getEmail());
        dto.setSection( teacher.getSection());
        dto.setGender(teacher.getGender());
        dto.setNiveau(teacher.getNiveau());
        dto.setPhoneNumber(teacher.getPhonenumber());
        dto.setSpeciality(teacher.getSpeciality());
        return dto;
       
    }
}

package com.school.platform.academic.application.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.ClasseRoomDTO;
import com.school.platform.academic.application.dto.TeacherDTO;
import com.school.platform.academic.application.interfaces.ClassRoomService;
import com.school.platform.academic.application.mapper.ClasseRoomMapper;
//import com.school.platform.academic.application.mapper.SectionMapper;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Section;
import com.school.platform.staff.application.interfaces.ITeachingStaffService;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.SectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassRoomServiceImpl implements ClassRoomService {

    private final ClasseRoomRepository classeRoomRepository;
    private final SectionRepository sectionRepository;
    private final ClasseRoomMapper mapper;
    private final ITeachingStaffService teachingStaffService;
   /// private final SectionMapper sectionMapper;

    public ClasseRoomDTO createClassRoom(ClasseRoomDTO dto) {
        ClasseRoom classeRoom = mapper.toEntity(dto);

        // Vérifie l'unicité du nom dans la section + année scolaire (pas globalement)
        Long sectionId = (classeRoom.getSection() != null) ? classeRoom.getSection().getId() : null;
        Long academicYearId = (classeRoom.getAcademicYear() != null) ? classeRoom.getAcademicYear().getId() : null;

        boolean alreadyExists;
        if (sectionId != null && academicYearId != null) {
            alreadyExists = classeRoomRepository.existsByNameClasseAndSectionIdAndAcademicYearId(
                    classeRoom.getNameClasse(), sectionId, academicYearId);
        } else if (sectionId != null) {
            alreadyExists = classeRoomRepository.existsByNameClasseAndSectionId(
                    classeRoom.getNameClasse(), sectionId);
        } else {
            alreadyExists = classeRoomRepository.existsByNameClasse(classeRoom.getNameClasse());
        }

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "Une classe avec le nom '" + classeRoom.getNameClasse()
                    + "' existe déjà dans cette section pour cette année scolaire.");
        }

        ClasseRoom saved = classeRoomRepository.save(classeRoom);
        return mapper.toDto(saved);
    }

    public ClasseRoomDTO getClassRoomById(Long id) {
        return classeRoomRepository.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "id", id));
    }

    public ClasseRoomDTO updateClassRoom(Long id, ClasseRoomDTO dto) {
        ClasseRoom existingclassroom = classeRoomRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Classe non trouver par ID:" + id));
        
        mapper.updateEntityFromDto(dto, existingclassroom);

        // 🟢 Mettre à jour la relation Teacher
    if (dto.getTeacher() != null && dto.getTeacher().getId() != null) {
        // Supposons que vous ayez un TeacherRepository pour trouver l'entité complète
        StaffMember newTeacher = teachingStaffService.getTeachingStaffMember(dto.getTeacher().getId());
        existingclassroom.setTeacher(newTeacher);
    } else {
        // Si l'ID est null, vous pourriez vouloir délier l'enseignant (mettre à null)
        existingclassroom.setTeacher(null); 
    }

        
        ClasseRoom updated = classeRoomRepository.save(existingclassroom);
        log.info("classroom mis a jour avec succes: {}", updated.getId());
        return mapper.toDto(updated);
    }

    public void deleteClassRoom(Long id) {

        if (!classeRoomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée avec l'id: " + id);
        }
        classeRoomRepository.deleteById(id);
    }

    
    public List<ClasseRoomDTO> getAllClassRooms() {
       return classeRoomRepository.findAllWithDetails().stream()
               .map(mapper::toDto)
               .collect(Collectors.toList());
    }

    public ClasseRoomDTO getClassRoomByNameClasse(String nameClasse) {

        // ClasseRoom entity = mapper.toEntity(nameClasse);
        // ClasseRoom classseByName = classeRoomRepository.findByNameClasse(entity);
        
        return classeRoomRepository.findByNameClasse(nameClasse).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "nameClasse", nameClasse));
    }

    public List<ClasseRoomDTO> getClassRoomsBySection(Long sectionId) {
        
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        return classeRoomRepository.findBySectionId(section.getId()).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public long getTotalClassRooms() {
        return classeRoomRepository.count();
    }

    public List<TeacherDTO> getAvailableTeachers() {
        Set<Long> assignedTeacherIds = classeRoomRepository.findAllWithDetails().stream()
                .map(ClasseRoom::getTeacher)
                .filter(teacher -> teacher != null && teacher.getId() != null)
                .map(StaffMember::getId)
                .collect(Collectors.toSet());

        return teachingStaffService.getTeachingStaffMembers().stream()
                .filter(teacher -> !assignedTeacherIds.contains(teacher.getId()))
                .map(this::toTeacherDto)
                .collect(Collectors.toList());
    }

    public void assignTeacher(Long classId, Long teacherId) {
        ClasseRoom classeRoom = classeRoomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "id", classId));
        StaffMember teacher = teachingStaffService.getTeachingStaffMember(teacherId);
        classeRoom.setTeacher(teacher);
        classeRoomRepository.save(classeRoom);
    }

    public void removeTeacher(Long classId) {
        ClasseRoom classeRoom = classeRoomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "id", classId));
        classeRoom.setTeacher(null);
        classeRoomRepository.save(classeRoom);
    }
    private TeacherDTO toTeacherDto(StaffMember staffMember) {
        return TeacherDTO.builder()
                .id(staffMember.getId())
                .firstnameTeacher(staffMember.getFirstnameTeacher())
                .lastnameTeacher(staffMember.getLastnameTeacher())
                .email(staffMember.getEmail())
                .phoneNumber(staffMember.getPhoneNumber())
                .gender(staffMember.getGender())
                .niveau(staffMember.getNiveau())
                .speciality(staffMember.getSpeciality())
                .adress(staffMember.getAdress())
                .dateEmbauche(staffMember.getDateEmbauche())
                .photoUrl(staffMember.getPhotoUrl())
                .cniNumber(staffMember.getCniNumber())
                .cniPhotoUrl(staffMember.getCniPhotoUrl())
                .build();
    }
}

package com.school.platform.academic.application.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.academic.application.dto.classeroom.ClasseRoomDTO;
import com.school.platform.academic.application.interfaces.ClassRoomService;
import com.school.platform.academic.application.mapper.ClasseRoomMapper;
import com.school.platform.academic.application.dto.section.SectionDTO;
import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Section;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import com.school.platform.staff.application.interfaces.ITeachingStaffService;
import com.school.platform.staff.application.mapper.StaffMemberMapper;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.staff.infrastructure.persistence.StaffMemberRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.SectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassRoomServiceImpl implements ClassRoomService {

    private final ClasseRoomRepository classeRoomRepository;
    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClasseRoomMapper mapper;
    private final ITeachingStaffService teachingStaffService;
    private final StaffMemberRepository staffMemberRepository;
    private final StaffMemberMapper staffMemberMapper;


    public ClasseRoomDTO createClassRoom(ClasseRoomDTO dto) {
        ClasseRoom classeRoom = mapper.toEntity(dto);

        // Safely resolve Section
        Section section = resolveSection(dto.getSection());
        classeRoom.setSection(section);

        // Safely resolve AcademicYear (with active year fallback)
        AcademicYear academicYear = resolveAcademicYear(dto.getAcademicYear());
        classeRoom.setAcademicYear(academicYear);

        // Safely resolve Teacher (StaffMember)
        StaffMember teacher = resolveTeacher(dto.getTeacher());
        classeRoom.setTeacher(teacher);

        Long sectionId = (section != null) ? section.getId() : null;
        Long academicYearId = (academicYear != null) ? academicYear.getId() : null;

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
                    + "' existe deja dans cette section pour cette annee scolaire.");
        }

        ClasseRoom saved = classeRoomRepository.save(classeRoom);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ClasseRoomDTO getClassRoomById(Long id) {
        return classeRoomRepository.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "id", id));
    }

    public ClasseRoomDTO updateClassRoom(Long id, ClasseRoomDTO dto) {
        ClasseRoom existingclassroom = classeRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe non trouver par ID:" + id));

        mapper.updateEntityFromDto(dto, existingclassroom);

        // Safely resolve Section
        if (dto.getSection() != null) {
            Section section = resolveSection(dto.getSection());
            if (section != null) {
                existingclassroom.setSection(section);
            }
        }

        // Safely resolve AcademicYear
        if (dto.getAcademicYear() != null) {
            AcademicYear academicYear = resolveAcademicYear(dto.getAcademicYear());
            if (academicYear != null) {
                existingclassroom.setAcademicYear(academicYear);
            }
        }

        // Safely resolve teacher
        if (dto.getTeacher() != null) {
            StaffMember teacher = resolveTeacher(dto.getTeacher());
            existingclassroom.setTeacher(teacher);
        } else {
            existingclassroom.setTeacher(null);
        }

        ClasseRoom updated = classeRoomRepository.save(existingclassroom);
        log.info("classeroom mis a jour avec succes: {}", updated.getId());
        return mapper.toDto(updated);
    }

    private Section resolveSection(SectionDTO sectionDTO) {
        if (sectionDTO == null) {
            return null;
        }
        if (sectionDTO.getLibelle() != null && !sectionDTO.getLibelle().isBlank()) {
            String libelle = sectionDTO.getLibelle().trim();
            Optional<Section> found = sectionRepository.findByLibelle(libelle);
            if (found.isPresent()) {
                return found.get();
            }
            List<Section> all = sectionRepository.findAll();
            for (Section s : all) {
                if (s.getLibelle() != null && s.getLibelle().trim().equalsIgnoreCase(libelle)) {
                    return s;
                }
            }
        }
        return null;
    }

    private AcademicYear resolveAcademicYear(AcademicYearDTO academicYearDTO) {
        if (academicYearDTO != null) {
            if (academicYearDTO.getLibelleAcademicYear() != null && !academicYearDTO.getLibelleAcademicYear().isBlank()) {
                String libelle = academicYearDTO.getLibelleAcademicYear().trim();
                Optional<AcademicYear> found = academicYearRepository.findByLibelleAcademicYear(libelle);
                if (found.isPresent()) {
                    return found.get();
                }
                List<AcademicYear> all = academicYearRepository.findAll();
                for (AcademicYear a : all) {
                    if (a.getLibelleAcademicYear() != null && a.getLibelleAcademicYear().trim().equalsIgnoreCase(libelle)) {
                        return a;
                    }
                }
            }
            if (academicYearDTO.getId() != null && academicYearDTO.getId() > 0) {
                Optional<AcademicYear> found = academicYearRepository.findById(academicYearDTO.getId());
                if (found.isPresent()) {
                    return found.get();
                }
            }
        }
        // Fallback: search for active academic year
        return academicYearRepository.findByStatutCode(true).orElse(null);
    }

    private StaffMember resolveTeacher(StaffMemberBasicDTO teacherDTO) {
        if (teacherDTO == null) {
            return null;
        }
        if (teacherDTO.getEmployeeNumber() != null && !teacherDTO.getEmployeeNumber().isBlank()) {
            Optional<StaffMember> found = staffMemberRepository.findByEmployeeNumberValue(teacherDTO.getEmployeeNumber().trim());
            if (found.isPresent()) {
                return found.get();
            }
        }
        if (teacherDTO.getId() != null && teacherDTO.getId() > 0) {
            Optional<StaffMember> found = staffMemberRepository.findById(teacherDTO.getId());
            if (found.isPresent()) {
                return found.get();
            }
            try {
                return teachingStaffService.getTeachingStaffMember(teacherDTO.getId());
            } catch (Exception ignored) { }
        }
        return null;
    }

    public void deleteClassRoom(Long id) {
        if (!classeRoomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvee avec l'id: " + id);
        }
        classeRoomRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ClasseRoomDTO> getAllClassRooms() {
        return classeRoomRepository.findAllWithDetails().stream()
               .map(mapper::toDto)
               .collect(Collectors.toList());
    }

    public ClasseRoomDTO getClassRoomByNameClasse(String nameClasse) {
        return classeRoomRepository.findByNameClasse(nameClasse).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "nameClasse", nameClasse));
    }

    @Transactional(readOnly = true)
    public List<ClasseRoomDTO> getClassRoomsBySection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        return classeRoomRepository.findBySectionId(section.getId()).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public long getTotalClassRooms() {
        return classeRoomRepository.count();
    }

    public List<StaffMemberBasicDTO> getAvailableTeachers() {
        Set<Long> assignedTeacherIds = classeRoomRepository.findAll().stream()
                .map(ClasseRoom::getTeacher)
                .filter(teacher -> teacher != null && teacher.getId() != null)
                .map(StaffMember::getId)
                .collect(Collectors.toSet());

        return teachingStaffService.getTeachingStaffMembers().stream()
                .filter(teacher -> !assignedTeacherIds.contains(teacher.getId()))
                .map(staffMemberMapper::toBasicDTO)
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



    @Override
    @Transactional(readOnly = true)
    public List<ClasseRoomDTO> getAllClassRooms(Long academicYearId) {
        if (academicYearId == null) {
            return getAllClassRooms();
        }
        return classeRoomRepository.findByAcademicYearId(academicYearId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClasseRoomDTO> getClassRoomsBySection(Long sectionId, Long academicYearId) {
        if (academicYearId == null) {
            return getClassRoomsBySection(sectionId);
        }
        return classeRoomRepository.findBySectionIdAndAcademicYearId(sectionId, academicYearId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffMemberBasicDTO> getTeachers() {
        return teachingStaffService.getTeachingStaffMembers().stream()
                .filter(member -> member != null)
                .map(staffMemberMapper::toBasicDTO)
                .collect(Collectors.toList());
    }
}

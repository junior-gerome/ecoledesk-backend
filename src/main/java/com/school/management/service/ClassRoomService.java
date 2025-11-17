package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.ClasseRoomDTO;
import com.school.management.mappers.ClasseRoomMapper;
//import com.school.management.mappers.SectionMapper;
import com.school.management.model.ClasseRoom;
import com.school.management.model.Section;
import com.school.management.model.Teacher;
import com.school.management.repository.ClasseRoomRepository;
import com.school.management.repository.SectionRepository;
import com.school.management.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassRoomService {

    private final ClasseRoomRepository classeRoomRepository;
    private final SectionRepository sectionRepository;
    private final ClasseRoomMapper mapper;
    private final TeacherRepository teacherRepository;
   /// private final SectionMapper sectionMapper;

    public ClasseRoomDTO createClassRoom(ClasseRoomDTO dto) {
        ClasseRoom classeRoom = mapper.toEntity(dto);
        if (classeRoomRepository.existsByNameClasse(classeRoom.getNameClasse())) {
            throw new IllegalArgumentException("La classe avec le nom " + classeRoom.getNameClasse() + " existe déjà.");
        }
        ClasseRoom saved = classeRoomRepository.save(classeRoom);
        return mapper.toDto(saved);
    }

    public ClasseRoomDTO getClassRoomById(Long id) {
        return classeRoomRepository.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "id", id));
    }

    public ClasseRoomDTO updateClassRoom(Long id, ClasseRoomDTO dto) {
        ClasseRoom existsById = classeRoomRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Classe non trouver par ID:" + id));
        
        //ClasseRoom classRoom = mapper.toEntity(dto);
        existsById.setCapacity(dto.getCapacity());
        existsById.setDescription(dto.getDescription());
        existsById.setNameClasse(dto.getNameClasse());

        // 🟢 Mettre à jour la relation Teacher
    if (dto.getTeacher() != null && dto.getTeacher().getId() != null) {
        // Supposons que vous ayez un TeacherRepository pour trouver l'entité complète
        Teacher newTeacher = teacherRepository.findById(dto.getTeacher().getId()) 
            .orElseThrow(() -> new ResourceNotFoundException("Enseignant", "id", dto.getTeacher().getId()));
        existsById.setTeacher(newTeacher);
    } else {
        // Si l'ID est null, vous pourriez vouloir délier l'enseignant (mettre à null)
        existsById.setTeacher(null); 
    }

        
        ClasseRoom updated = classeRoomRepository.save(existsById);
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
}

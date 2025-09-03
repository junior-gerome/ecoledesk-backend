package com.school.management.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.school.exception.ResourceNotFoundException;
import com.school.management.model.ClasseRoom;
import com.school.management.model.Section;
import com.school.management.repository.ClasseRoomRepository;
import com.school.management.repository.SectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassRoomService {

    private final ClasseRoomRepository classeRoomRepository;
    private final SectionRepository sectionRepository;

    public ClasseRoom createClassRoom(ClasseRoom classeRoom) {
        if (classeRoomRepository.existsByNameClasse(classeRoom.getNameClasse())) {
            throw new IllegalArgumentException("La classe avec le nom " + classeRoom.getNameClasse() + " existe déjà.");
        }
        return classeRoomRepository.save(classeRoom);
    }

    public ClasseRoom getClassRoomById(Long id) {
        return classeRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "id", id));
    }

    public ClasseRoom updateClassRoom(Long id, ClasseRoom updatedClassRoom) {
        ClasseRoom existingClassRoom = getClassRoomById(id);
        if (!existingClassRoom.getNameClasse().equals(updatedClassRoom.getNameClasse())) {
            if (classeRoomRepository.existsByNameClasse(updatedClassRoom.getNameClasse())) {
                throw new IllegalArgumentException("La classe avec le nom " + updatedClassRoom.getNameClasse() + " existe déjà.");
            }
        }

        existingClassRoom.setNameClasse(updatedClassRoom.getNameClasse());
        existingClassRoom.setLevel(updatedClassRoom.getLevel());
        existingClassRoom.setSection(updatedClassRoom.getSection());
        existingClassRoom.setCapacity(updatedClassRoom.getCapacity());
        existingClassRoom.setTeacher(updatedClassRoom.getTeacher());
        existingClassRoom.setAnneeScolaire(updatedClassRoom.getAnneeScolaire());
        existingClassRoom.setDescription(updatedClassRoom.getDescription());

        return classeRoomRepository.save(existingClassRoom);
    }

    public void deleteClassRoom(Long id) {
        ClasseRoom existingClassRoom = getClassRoomById(id);
        classeRoomRepository.delete(existingClassRoom);
    }

    public List<ClasseRoom> getAllClassRooms() {
        return classeRoomRepository.findAll();
    }

    public ClasseRoom getClassRoomByNameClasse(String nameClasse) {
        return classeRoomRepository.findByNameClasse(nameClasse)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseRoom", "nameClasse", nameClasse));
    }

    public List<ClasseRoom> getClassRoomsBySection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
        return classeRoomRepository.findBySectionId(section.getId());
    }
}

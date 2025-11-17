package com.school.service.impl;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.ClasseRoomDTO;
import com.school.management.mappers.ClasseRoomMapper;
import com.school.management.model.ClasseRoom;
import com.school.management.repository.ClasseRoomRepository;
import com.school.service.ClassService;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClasseRoomRepository classeRepository;
    private final ClasseRoomMapper mapper;

    @Override
    public List<ClasseRoomDTO> getAllClasses() {
        return classeRepository.findAll().stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    public Optional<ClasseRoomDTO> getClassById(Long id) {
        return classeRepository.findById(id)
            .map(mapper::toDto);
    }

    @Override
    public ClasseRoomDTO createClass(ClasseRoomDTO dto) {
        ClasseRoom classe = mapper.toEntity(dto);
        ClasseRoom saved = classeRepository.save(classe);
        return mapper.toDto(saved);
    }

    @Override
    public ClasseRoomDTO updateClass(Long id, ClasseRoomDTO dto) {
        if (!classeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée avec l'id: " + id);
        }
        ClasseRoom classe = mapper.toEntity(dto);
        classe.setId(id);
        ClasseRoom updated = classeRepository.save(classe);
        return mapper.toDto(updated);
    }

    @Override
    public void deleteClass(Long id) {
        if (!classeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée avec l'id: " + id);
        }
        classeRepository.deleteById(id);
    }

    // private ClasseRoomDTO convertToDTO(ClasseRoom classe) {
    //     ClasseRoomDTO dto = new ClasseRoomDTO();
    //     dto.setId(classe.getId());
    //     dto.setNameClasse(classe.getNameClasse());
    //     dto.setLevell(classe.getLevel());
    //     dto.setCapacity(classe.getCapacity());
    //     dto.setAnneeScolaire(classe.getAnneeScolaire());
    //     dto.setDescription(classe.getDescription());
    //    // dto.setActive(classe.getActif());
    //     return dto;
    // }

    // private ClasseRoom convertToEntity(ClasseRoomDTO dto) {
    //     ClasseRoom classe = new ClasseRoom();
    //     classe.setId(dto.getId());
    //     classe.setNameClasse(dto.getNameClasse());
    //     classe.setLevel(dto.getLevell());
    //     classe.setCapacity(dto.getCapacity());
    //     classe.setAnneeScolaire(dto.getAnneeScolaire());
    //     classe.setDescription(dto.getDescription());
    //     //classe.setActif(dto.getActive());
    //     return classe;
    // }
}

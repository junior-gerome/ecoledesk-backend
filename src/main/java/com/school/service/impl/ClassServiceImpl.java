package com.school.service.impl;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.ClassDTO;
import com.school.management.model.Classe;
import com.school.management.repository.ClasseRepository;
import com.school.service.ClassService;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClasseRepository classeRepository;

    @Override
    public List<ClassDTO> getAllClasses() {
        return classeRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
    }

    @Override
    public Optional<ClassDTO> getClassById(Long id) {
        return classeRepository.findById(id)
            .map(this::convertToDTO);
    }

    @Override
    public ClassDTO createClass(ClassDTO classDTO) {
        Classe classe = convertToEntity(classDTO);
        return convertToDTO(classeRepository.save(classe));
    }

    @Override
    public ClassDTO updateClass(Long id, ClassDTO classDTO) {
        if (!classeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée avec l'id: " + id);
        }
        Classe classe = convertToEntity(classDTO);
        classe.setId(id);
        return convertToDTO(classeRepository.save(classe));
    }

    @Override
    public void deleteClass(Long id) {
        if (!classeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classe non trouvée avec l'id: " + id);
        }
        classeRepository.deleteById(id);
    }

    private ClassDTO convertToDTO(Classe classe) {
        ClassDTO dto = new ClassDTO();
        dto.setId(classe.getId());
        dto.setNameClasse(classe.getNameClasse());
        dto.setLevell(classe.getLevel());
        dto.setCapacity(classe.getCapacite());
        dto.setAnneeScolaire(classe.getAnneeScolaire());
        dto.setDescription(classe.getDescription());
        dto.setActive(classe.getActif());
        return dto;
    }

    private Classe convertToEntity(ClassDTO dto) {
        Classe classe = new Classe();
        classe.setId(dto.getId());
        classe.setNameClasse(dto.getNameClasse());
        classe.setLevel(dto.getLevell());
        classe.setCapacite(dto.getCapacity());
        classe.setAnneeScolaire(dto.getAnneeScolaire());
        classe.setDescription(dto.getDescription());
        classe.setActif(dto.getActive());
        return classe;
    }
}

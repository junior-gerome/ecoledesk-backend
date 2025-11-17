package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.school.management.dto.ParentDTO;
import com.school.management.mappers.ParentMapper;
import com.school.management.model.Parent;
import com.school.management.repository.ParentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentMapper mapper;

    @Transactional
    public ParentDTO createParent(ParentDTO dto){
        Parent entity = mapper.toEntity(dto);
        Parent saved = parentRepository.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional
    public List<ParentDTO> getAllParents() {
        return parentRepository.findAll().stream().map( mapper :: toDto).collect(Collectors.toList());
    }

    // @Transactional
    // public Parent createParent(Parent parent) {
    //     return parentRepository.save(parent);
    // }

    @Transactional
    public ParentDTO updateParent(Long id, ParentDTO dto) {
        Parent existingParent = parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouvé avec l'ID " + id));
        
        Parent entity = mapper.toEntity(dto);
                entity.setId(existingParent.getId());
               // entity.setRegistrationDate(existingParent.getRegistrationDate());

        // Ne mettre à jour que les champs fournis (non null) dans l'objet 'parent' reçu
        // if (dto.getFirstNameParent() != null) {
        //     existingParent.setFirstNameParent(dto.getFirstNameParent());
        // }
        // if (dto.getLastNameParent() != null) {
        //     existingParent.setLastNameParent(dto.getLastNameParent());
        // }
        // if (dto.getPhoneNumber() != null) {
        //     existingParent.setPhoneNumber(dto.getPhoneNumber());
        // }
        // if (dto.getEmail() != null) {
        //     existingParent.setEmail(dto.getEmail());
        // }
        // if (dto.getProfessionParent() != null) {
        //     existingParent.setProfessionParent(dto.getProfessionParent());
        // }
        // if (dto.getTypeParent() != null) {
        //     existingParent.setTypeParent(dto.getTypeParent());
        // }
        // if (dto.getAddress() != null) {
        //     existingParent.setAddress(dto.getAddress());
        // }

        Parent updated = parentRepository.save(entity);

        return mapper.toDto(updated);
    }

    @Transactional
    public void deleteParent(Long id) {
        Parent existingParent = parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouvé avec l'ID " + id));
        parentRepository.delete(existingParent);
    }

    @Transactional
    public ParentDTO getParentById(Long id) {
        return parentRepository.findById(id).map(mapper:: toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouvé avec l'ID " + id));
    }

}

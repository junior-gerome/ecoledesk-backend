package com.school.management.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.school.management.model.Parent;
import com.school.management.repository.ParentRepository;

import jakarta.transaction.Transactional;

@Service
public class ParentService {

    private final ParentRepository parentRepository;

    public ParentService(ParentRepository parentRepository) {
        this.parentRepository = parentRepository;
    }

    @Transactional
    public List<Parent> getAllParents() {
        return parentRepository.findAll();
    }

    @Transactional
    public Parent createParent(Parent parent) {
        return parentRepository.save(parent);
    }

    @Transactional
    public Parent updateParent(Long id, Parent parent) {
        Parent existingParent = parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouvé avec l'ID " + id));

        // Ne mettre à jour que les champs fournis (non null) dans l'objet 'parent' reçu
        if (parent.getFirstNameParent() != null) {
            existingParent.setFirstNameParent(parent.getFirstNameParent());
        }
        if (parent.getLastNameParent() != null) {
            existingParent.setLastNameParent(parent.getLastNameParent());
        }
        if (parent.getPhonenumber() != null) {
            existingParent.setPhonenumber(parent.getPhonenumber());
        }
        if (parent.getEmail() != null) {
            existingParent.setEmail(parent.getEmail());
        }
        if (parent.getProfessionParent() != null) {
            existingParent.setProfessionParent(parent.getProfessionParent());
        }
        if (parent.getTypeParent() != null) {
            existingParent.setTypeParent(parent.getTypeParent());
        }
        if (parent.getAddress() != null) {
            existingParent.setAddress(parent.getAddress());
        }

        return parentRepository.save(existingParent);
    }

    @Transactional
    public void deleteParent(Long id) {
        Parent existingParent = parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouvé avec l'ID " + id));
        parentRepository.delete(existingParent);
    }

    public Parent getParentById(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parent non trouvé avec l'ID " + id));
    }

}

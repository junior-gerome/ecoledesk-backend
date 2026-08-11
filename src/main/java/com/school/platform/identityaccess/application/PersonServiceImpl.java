package com.school.platform.identityaccess.application;

import com.school.platform.identityaccess.application.dto.person.*;
import com.school.platform.identityaccess.application.mapper.PersonMapper;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Legacy class kept for reference - superseded by application.impl.PersonServiceImpl
// @Service
@RequiredArgsConstructor
@Transactional
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonFullDTO create(PersonFullDTO dto) {
        if (personRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email déjà utilisé");
        }
        Person person = personMapper.toEntity(dto);
        person = personRepository.save(person);
        return personMapper.toFullDTO(person);
    }

    public PersonFullDTO update(Long id, PersonFullDTO dto) {
        Person person = personRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmail(dto.getEmail());
        person.setPhone(dto.getPhone());
        person.setBirthDate(dto.getBirthDate());
        person.setGender(dto.getGender());
        person.setAddress(dto.getAddress());
        person.setCity(dto.getCity());
        person.setCountry(dto.getCountry());
        person.setPhotoUrl(dto.getPhotoUrl());
        
        person = personRepository.save(person);
        return personMapper.toFullDTO(person);
    }

    @Transactional(readOnly = true)
    public PersonFullDTO findById(Long id) {
        Person person = personRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        return personMapper.toFullDTO(person);
    }

    @Transactional(readOnly = true)
    public Page<PersonMediumDTO> findAll(Pageable pageable) {
        return personRepository.findAll(pageable)
            .map(personMapper::toMediumDTO);
    }

    public void delete(Long id) {
        Person person = personRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        person.setActive(false);
        personRepository.save(person);
    }
}

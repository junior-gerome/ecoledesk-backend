package com.school.platform.identityaccess.application.impl;

import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import com.school.platform.identityaccess.application.interfaces.IPersonService;
import com.school.platform.identityaccess.application.mapper.PersonMapper;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonServiceImpl implements IPersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public PersonFullDTO create(PersonFullDTO dto) {
        if (personRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email déjà utilisé");
        }
        Person person = personMapper.toEntity(dto);
        return personMapper.toFullDTO(personRepository.save(person));
    }

    @Override
    public PersonFullDTO update(Long id, PersonFullDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        personMapper.updateEntity(dto, person);
        return personMapper.toFullDTO(personRepository.save(person));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonFullDTO findById(Long id) {
        return personMapper.toFullDTO(personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Personne non trouvée")));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonMediumDTO> findAll(Pageable pageable) {
        return personRepository.findAll(pageable).map(personMapper::toMediumDTO);
    }

    @Override
    public void delete(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        person.setActive(false);
        personRepository.save(person);
    }
}

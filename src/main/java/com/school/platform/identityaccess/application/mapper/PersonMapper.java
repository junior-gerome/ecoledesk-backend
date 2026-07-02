package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.person.*;
import com.school.platform.identityaccess.domain.model.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {

    public PersonBasicDTO toBasicDTO(Person person) {
        if (person == null) return null;
        
        PersonBasicDTO dto = new PersonBasicDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmail(person.getEmail());
        dto.setPhone(person.getPhone());
        dto.setGender(person.getGender());
        return dto;
    }

    public PersonMediumDTO toMediumDTO(Person person) {
        if (person == null) return null;
        
        PersonMediumDTO dto = new PersonMediumDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmail(person.getEmail());
        dto.setPhone(person.getPhone());
        dto.setBirthDate(person.getBirthDate());
        dto.setGender(person.getGender());
        dto.setAddress(person.getAddress());
        dto.setCity(person.getCity());
        dto.setActive(person.getActive());
        return dto;
    }

    public PersonFullDTO toFullDTO(Person person) {
        if (person == null) return null;
        
        PersonFullDTO dto = new PersonFullDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmail(person.getEmail());
        dto.setPhone(person.getPhone());
        dto.setBirthDate(person.getBirthDate());
        dto.setGender(person.getGender());
        dto.setAddress(person.getAddress());
        dto.setCity(person.getCity());
        dto.setCountry(person.getCountry());
        dto.setPhotoUrl(person.getPhotoUrl());
        dto.setActive(person.getActive());
        dto.setCreationDate(person.getCreationDate());
        dto.setUpdateDate(person.getUpdateDate());
        return dto;
    }

    public Person toEntity(PersonFullDTO dto) {
        if (dto == null) return null;
        
        Person person = new Person();
        person.setId(dto.getId());
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
        person.setActive(dto.getActive());
        return person;
    }
}

package com.school.gestionuser.application.mapper;

import com.school.gestionuser.domain.model.identity.Person;
import com.school.gestionuser.infrastructure.web.dto.PersonResponse;

public class PersonMapper {
    public PersonResponse toResponse(Person person) {
        return PersonResponse.from(person);
    }
}
package com.school.gestionuser.domain.service;

import com.school.gestionuser.domain.model.identity.Person;

public class PersonDomainService {
    public String fullName(Person person) {
        if (person == null) {
            return "";
        }
        return String.join(" ", person.getFirstName(), person.getLastName()).trim();
    }
}
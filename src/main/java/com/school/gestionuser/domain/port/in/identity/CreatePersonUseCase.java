package com.school.gestionuser.domain.port.in.identity;

import com.school.gestionuser.application.dto.command.CreatePersonCommand;
import com.school.gestionuser.domain.model.identity.Person;

public interface CreatePersonUseCase {
    Person createPerson(CreatePersonCommand command);
}
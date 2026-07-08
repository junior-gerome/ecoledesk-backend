// com.school.gestionuser.domain.port.out.PersonRepositoryPort
package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.identity.Person;
import java.util.Optional;
import java.util.List;

public interface PersonRepositoryPort {
    Person save(Person person);
    Optional<Person> findById(String id);
    Optional<Person> findByEmail(String email);
    Optional<Person> findByNationalId(String nationalId);
    boolean existsByEmail(String email);
    List<Person> findAll(int page, int size);
    void delete(String id);
}

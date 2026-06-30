package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.identity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    Optional<User> findByPersonId(String personId);
    List<User> findAll();
    User save(User user);
    void delete(User user);
}
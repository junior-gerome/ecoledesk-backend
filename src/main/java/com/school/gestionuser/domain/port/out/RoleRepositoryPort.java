package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.identity.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findById(String id);
    Optional<Role> findByCode(String code);
    List<Role> findAll();
    Role save(Role role);
    void delete(Role role);
}
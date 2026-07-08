package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.identity.Permission;
import java.util.List;
import java.util.Optional;

public interface PermissionRepositoryPort {
    Optional<Permission> findById(String id);
    Optional<Permission> findByCode(String code);
    List<Permission> findAll();
    Permission save(Permission permission);
    void delete(Permission permission);
}
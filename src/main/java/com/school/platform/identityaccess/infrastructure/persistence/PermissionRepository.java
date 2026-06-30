package com.school.platform.identityaccess.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);
    Optional<Permission> findByCodeIgnoreCase(String code);
    boolean existsByCode(String code);
    boolean existsByCodeIgnoreCase(String code);
}
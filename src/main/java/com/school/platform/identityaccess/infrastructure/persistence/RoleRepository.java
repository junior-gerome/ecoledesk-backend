package com.school.platform.identityaccess.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    Optional<Role> findByCodeIgnoreCase(String code);
    boolean existsByCode(String code);
    boolean existsByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findWithPermissionsByCode(String code);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(Long id);
}
package com.school.platform.identityaccess.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.Users;
import com.school.platform.identityaccess.domain.model.UsersProfil;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    @EntityGraph(attributePaths = {"profils", "profils.role", "profils.role.permissions"})
    Optional<Users> findByUsername(String username);

    List<Users> findByProfils(List<UsersProfil> profils);

    @Override
    @EntityGraph(attributePaths = {"profils", "profils.role", "profils.role.permissions"})
    List<Users> findAll();

    List<Users> findAllById(Iterable<Long> ids);

    boolean existsByUsername(String username);

    List<Users> findByActifTrue();
}
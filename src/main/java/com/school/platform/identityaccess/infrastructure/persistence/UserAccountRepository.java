package com.school.platform.identityaccess.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.UserAccount;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @EntityGraph(attributePaths = {"person", "roles", "roles.permissions"})
    Optional<UserAccount> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"person", "roles", "roles.permissions"})
    List<UserAccount> findAll();

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<UserAccount> findByEnabledTrue();
}

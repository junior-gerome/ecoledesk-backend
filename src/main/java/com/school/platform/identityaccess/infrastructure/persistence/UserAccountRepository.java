package com.school.platform.identityaccess.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.domain.model.UserAccountStatus;

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

    @Query(value = """
            SELECT DISTINCT u FROM UserAccount u
            JOIN u.person p
            LEFT JOIN u.roles r
            WHERE (:search IS NULL
                    OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:roleCode IS NULL OR UPPER(r.code) = UPPER(:roleCode))
              AND (:status IS NULL OR u.status = :status)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u) FROM UserAccount u
            JOIN u.person p
            LEFT JOIN u.roles r
            WHERE (:search IS NULL
                    OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:roleCode IS NULL OR UPPER(r.code) = UPPER(:roleCode))
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<UserAccount> search(@Param("search") String search,
                             @Param("roleCode") String roleCode,
                             @Param("status") UserAccountStatus status,
                             Pageable pageable);
}

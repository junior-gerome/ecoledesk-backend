package com.school.platform.identityaccess.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.domain.model.UsersProfil;

@Repository
public interface UsersProfilRepository extends JpaRepository<UsersProfil, Long> {
    List<UsersProfil> findByUserId(Long userId);
    List<UsersProfil> findByRoleType(RoleType roleType);
    Optional<UsersProfil> findByUserIdAndRoleType(Long userId, RoleType roleType);
    Optional<UsersProfil> findByReferenceIdAndRoleType(Long referenceId, RoleType roleType);
}

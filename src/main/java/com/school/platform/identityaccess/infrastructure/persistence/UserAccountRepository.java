package com.school.platform.identityaccess.infrastructure.persistence;

import com.school.platform.identityaccess.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);
    
    @Query("SELECT ua FROM UserAccount ua JOIN FETCH ua.roles WHERE ua.username = :username")
    Optional<UserAccount> findByUsernameWithRoles(String username);
}

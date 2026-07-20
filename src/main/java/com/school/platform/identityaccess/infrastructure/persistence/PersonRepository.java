package com.school.platform.identityaccess.infrastructure.persistence;

import com.school.platform.identityaccess.domain.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Query("select p from Person p where lower(p.email.value) = lower(:email)")
    Optional<Person> findByEmail(@Param("email") String email);

    @Query("select count(p) > 0 from Person p where lower(p.email.value) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);
}

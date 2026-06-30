package com.school.platform.enrollment.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.school.platform.enrollment.domain.model.Parent;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    @Query("select p from Parent p join p.person person where lower(person.email) = lower(:email)")
    Optional<Parent> findByEmail(@Param("email") String email);

    @Query("select p from Parent p join p.person person where person.phone = :phoneNumber")
    Optional<Parent> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
package com.school.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.enums.TypeAffectation;

import java.util.Optional;

@Repository
public interface TypeAffectationRepository extends JpaRepository<TypeAffectation, String> {
    Optional<TypeAffectation> findByLibelle(String libelle);
}

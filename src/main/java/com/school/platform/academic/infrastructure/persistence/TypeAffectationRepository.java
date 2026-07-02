package com.school.platform.academic.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.academic.domain.model.TypeAffectation;

import java.util.Optional;

@Repository
public interface TypeAffectationRepository extends JpaRepository<TypeAffectation, String> {
    Optional<TypeAffectation> findByLibelle(String libelle);
}

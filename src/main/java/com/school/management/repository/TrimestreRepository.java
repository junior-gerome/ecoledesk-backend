package com.school.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.model.Trimestre;

@Repository
public interface TrimestreRepository extends JpaRepository<Trimestre, Long> {
    List<Trimestre> findByAnneeScolaireId(Long anneeScolaireId);
    Optional<Trimestre> findByLibelleTrimestre(String libelleTrimestre);
    boolean existsByLibelleTrimestre(String libelleTrimestre);
}

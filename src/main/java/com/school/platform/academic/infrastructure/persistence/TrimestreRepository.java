package com.school.platform.academic.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.academic.domain.model.Trimestre;

@Repository
public interface TrimestreRepository extends JpaRepository<Trimestre, Long> {
    List<Trimestre> findByAnneeScolaireId(Long anneeScolaireId);
    Optional<Trimestre> findByLibelleTrimestre(String libelleTrimestre);
    boolean existsByLibelleTrimestre(String libelleTrimestre);
}

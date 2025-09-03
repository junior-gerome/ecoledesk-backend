package com.school.management.repository;

import com.school.management.model.AnneeScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, Long> {
    Optional<AnneeScolaire> findByStatutCode(boolean statutCode);
    boolean existsByLibelleAnneeScolaire(String libelleAnneeScolaire);
}

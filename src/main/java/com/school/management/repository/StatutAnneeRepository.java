package com.school.management.repository;

import com.school.management.model.StatutAnnee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StatutAnneeRepository extends JpaRepository<StatutAnnee, String> {
    Optional<StatutAnnee> findByLibelle(String libelle);
}

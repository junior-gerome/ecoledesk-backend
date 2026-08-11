package com.school.platform.academic.infrastructure.persistence;

import com.school.platform.academic.domain.model.StatutYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StatutYearRepository extends JpaRepository<StatutYear, String> {
    Optional<StatutYear> findByLibelle(String libelle);
}

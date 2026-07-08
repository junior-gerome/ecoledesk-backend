package com.school.platform.academic.infrastructure.persistence;

import com.school.platform.academic.domain.model.AnneeScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, Long> {
    Optional<AnneeScolaire> findByStatutCode(boolean statutCode);
    boolean existsByLibelleAnneeScolaire(String libelleAnneeScolaire);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AnneeScolaire a where a.id = :id")
    Optional<AnneeScolaire> findByIdForUpdate(Long id);

    @Modifying
    @Query("update AnneeScolaire a set a.statutCode = false where a.statutCode = true")
    int deactivateActiveYears();
}

package com.school.platform.academic.infrastructure.persistence;

import com.school.platform.academic.domain.model.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByStatutCode(boolean statutCode);
    Optional<AcademicYear> findByLibelleAcademicYear(String libelleAcademicYear);
    boolean existsByLibelleAcademicYear(String libelleAcademicYear);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AcademicYear a where a.id = :id")
    Optional<AcademicYear> findByIdForUpdate(Long id);

    @Modifying
    @Query("update AcademicYear a set a.statutCode = false where a.statutCode = true")
    int deactivateActiveYears();
}

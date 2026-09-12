package com.school.platform.academic.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.academic.domain.model.Trimestre;

@Repository
public interface TrimestreRepository extends JpaRepository<Trimestre, Long> {
    @Query("SELECT t FROM Trimestre t LEFT JOIN FETCH t.academicYear")
    List<Trimestre> findAllWithAcademicYear();

    List<Trimestre> findByAcademicYearId(Long AcademicYearId);
    @Query("SELECT t FROM Trimestre t LEFT JOIN FETCH t.academicYear WHERE t.id = :id")
    Optional<Trimestre> findByIdWithAcademicYear(Long id);

    Optional<Trimestre> findByLibelleTrimestre(String libelleTrimestre);
    boolean existsByLibelleTrimestre(String libelleTrimestre);
}

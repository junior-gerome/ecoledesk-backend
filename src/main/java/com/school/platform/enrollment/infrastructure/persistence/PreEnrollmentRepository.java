package com.school.platform.enrollment.infrastructure.persistence;

import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreEnrollmentRepository extends JpaRepository<PreEnrollment, Long> {

    boolean existsByNumberValue(String number);

    Optional<PreEnrollment> findByNumberValue(String number);

    List<PreEnrollment> findByStatus(PreEnrollmentStatus status);

    Page<PreEnrollment> findByStatus(PreEnrollmentStatus status, Pageable pageable);

    /**
     * Version paginée filtrée par statut avec JOIN FETCH pour éviter LazyInitializationException.
     */
    @Query(value = "SELECT p FROM PreEnrollment p JOIN FETCH p.academicYear WHERE p.status = :status",
           countQuery = "SELECT COUNT(p) FROM PreEnrollment p WHERE p.status = :status")
    Page<PreEnrollment> findAllWithAcademicYearByStatus(
            @Param("status") PreEnrollmentStatus status,
            Pageable pageable);

    /**
     * Charge toutes les préinscriptions avec leur année scolaire en un seul JOIN FETCH
     * pour éviter les LazyInitializationException lors du mapping (MediumDTO utilise academicYear.libelleAcademicYear).
     */
    @Query("SELECT p FROM PreEnrollment p JOIN FETCH p.academicYear ORDER BY p.creationDate DESC")
    List<PreEnrollment> findAllWithAcademicYear();

    /**
     * Version paginée avec JOIN FETCH pour éviter N+1 et LazyInitializationException.
     * countQuery séparé obligatoire quand un JOIN FETCH est présent.
     */
    @Query(value = "SELECT p FROM PreEnrollment p JOIN FETCH p.academicYear",
           countQuery = "SELECT COUNT(p) FROM PreEnrollment p")
    Page<PreEnrollment> findAllWithAcademicYear(Pageable pageable);

    /**
     * Version paginée avec JOIN FETCH, filtre par statut et recherche libre
     * (numéro de dossier, nom/prénom du candidat, niveau demandé).
     * Search et status sont optionnels ; countQuery séparé obligatoire.
     */
    @Query(value = """
            SELECT p FROM PreEnrollment p JOIN FETCH p.academicYear
            WHERE (:status IS NULL OR p.status = :status)
              AND (:search IS NULL OR :search = ''
                  OR LOWER(COALESCE(p.number.value, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(p.applicant.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(p.applicant.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(p.requestedLevel, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """,
           countQuery = """
            SELECT COUNT(p) FROM PreEnrollment p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:search IS NULL OR :search = ''
                  OR LOWER(COALESCE(p.number.value, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(p.applicant.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(p.applicant.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(p.requestedLevel, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<PreEnrollment> findAllWithAcademicYearAndSearch(
            @Param("search") String search,
            @Param("status") PreEnrollmentStatus status,
            Pageable pageable);

    @Query("SELECT p FROM PreEnrollment p WHERE p.academicYear.id = :academicYearId ORDER BY p.creationDate DESC")
    List<PreEnrollment> findByAcademicYearId(@Param("academicYearId") Long academicYearId);

    @Query("SELECT p FROM PreEnrollment p WHERE p.academicYear.id = :academicYearId AND p.status = :status")
    List<PreEnrollment> findByAcademicYearIdAndStatus(
            @Param("academicYearId") Long academicYearId,
            @Param("status") PreEnrollmentStatus status);
}

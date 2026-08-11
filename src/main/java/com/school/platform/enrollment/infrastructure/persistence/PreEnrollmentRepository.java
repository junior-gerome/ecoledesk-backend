package com.school.platform.enrollment.infrastructure.persistence;

import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
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

    @Query("SELECT p FROM PreEnrollment p WHERE p.academicYear.id = :academicYearId ORDER BY p.creationDate DESC")
    List<PreEnrollment> findByAcademicYearId(@Param("academicYearId") Long academicYearId);

    @Query("SELECT p FROM PreEnrollment p WHERE p.academicYear.id = :academicYearId AND p.status = :status")
    List<PreEnrollment> findByAcademicYearIdAndStatus(
            @Param("academicYearId") Long academicYearId,
            @Param("status") PreEnrollmentStatus status);
}

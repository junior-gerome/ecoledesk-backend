package com.school.platform.enrollment.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByPreEnrollmentId(Long preEnrollmentId);

    Optional<Enrollment> findByPreEnrollmentId(Long preEnrollmentId);

    long countByClassroomIdAndAcademicYearIdAndStatus(Long classroomId, Long academicYearId, EnrollmentStatus status);

    Optional<Enrollment> findTopByStudentIdAndStatusOrderByEnrollmentDateDesc(Long studentId, EnrollmentStatus status);

    List<Enrollment> findByClassroomIdAndStatus(Long classroomId, EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);

    long countByClassroomIdAndStatus(Long classroomId, EnrollmentStatus status);

    long countByAcademicYearIdAndStatus(Long academicYearId, EnrollmentStatus status);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    @Query("""
            select e from Enrollment e
            join fetch e.student s
            where e.classroom.id = :classroomId
              and e.status = :status
            """)
    Page<Enrollment> findByClassroomIdAndStatus(
            @Param("classroomId") Long classroomId,
            @Param("status") EnrollmentStatus status,
            Pageable pageable);

    @Query("""
            select e from Enrollment e
            join fetch e.student s
            join fetch s.person p
            where e.classroom.id = :classroomId
              and e.status = :status
              and (:academicYearId is null or e.academicYear.id = :academicYearId)
            order by p.lastName asc, p.firstName asc
            """)
    List<Enrollment> findRosterByClassroom(
            @Param("classroomId") Long classroomId,
            @Param("status") EnrollmentStatus status,
            @Param("academicYearId") Long academicYearId);
}

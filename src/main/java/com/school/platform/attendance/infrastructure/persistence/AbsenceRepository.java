package com.school.platform.attendance.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.platform.attendance.domain.model.Absence;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    int countByStudentId(Long studentId);
    long countByDate(LocalDate date);
    List<Absence> findByStudentIdInAndDate(List<Long> studentIds, LocalDate date);
    List<Absence> findByStudentIdInAndDateBetween(List<Long> studentIds, LocalDate startDate, LocalDate endDate);
    List<Absence> findByDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<Absence> findByStudentIdAndDate(Long studentId, LocalDate date);
}

package com.school.management.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.management.model.Absence;

@Repository

public interface AbsenceRepository extends JpaRepository<Absence, Long> {
  int countByStudentId(Long studentId); // Nom de méthode corrigé
}

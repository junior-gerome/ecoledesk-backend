package com.school.platform.academic.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.academic.domain.model.Subject;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    // Méthodes de base fournies par JpaRepository
    Optional<Subject> findByNameSubject(String nameSubject);
    boolean existsByNameSubject(String nameSubject);
    boolean existsByCode(String code);
}

package com.school.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.model.Subject;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    // Méthodes de base fournies par JpaRepository
    Optional<Subject> findByNameSubject(String nameSubject);
    boolean existsByNameSubject(String nameSubject);
}

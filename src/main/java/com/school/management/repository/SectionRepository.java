package com.school.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.model.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByLibelle(String libelle);
}

package com.school.platform.academic.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.academic.domain.model.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByLibelle(String libelle);
}

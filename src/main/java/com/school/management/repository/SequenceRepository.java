package com.school.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.model.Sequence;

@Repository
public interface SequenceRepository extends JpaRepository<Sequence, Long> {
    List<Sequence> findByTrimestreId(Long trimestreId);
    Optional<Sequence> findByLibelleSequence(String libelleSequence);
    boolean existsByLibelleSequence(String libelleSequence);
}

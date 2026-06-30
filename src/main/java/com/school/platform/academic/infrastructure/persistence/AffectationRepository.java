package com.school.platform.academic.infrastructure.persistence;

import com.school.platform.academic.domain.model.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findByTeacherId(Long teacherId);
    List<Affectation> findByClasseId(Long classeId);
    List<Affectation> findBySubjectId(Long subjectId);
    List<Affectation> findByAnneeScolaireId(Long anneeScolaireId);
    boolean existsByTeacherIdAndClasseIdAndSubjectIdAndAnneeScolaireId(
        Long teacherId, Long classeId, Long subjectId, Long anneeScolaireId);
    Optional<Affectation> findByTeacherIdAndClasseIdAndSubjectIdAndAnneeScolaireId(
        Long teacherId, Long classeId, Long subjectId, Long anneeScolaireId);
}

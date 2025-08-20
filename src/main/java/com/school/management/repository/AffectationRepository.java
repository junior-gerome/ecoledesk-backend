package com.school.management.repository;

import com.school.management.model.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    List<Affectation> findByTeacherId(Long teacherId);
    List<Affectation> findByClasseId(Long classeId);
    List<Affectation> findBySubjectId(Long subjectId);
    List<Affectation> findByAnneeScolaireId(Long anneeScolaireId);
    boolean existsByTeacherIdAndClasseIdAndSubjectIdAndAnneeScolaireId(
        Long teacherId, Long classeId, Long subjectId, Long anneeScolaireId);
}

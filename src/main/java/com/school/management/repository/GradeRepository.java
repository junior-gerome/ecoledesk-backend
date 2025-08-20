package com.school.management.repository;

import com.school.management.model.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
        List<Grade> findByStudentId(Long studentId);

        List<Grade> findByClasseId(Long classId);
        List<Grade> findBySubjectId(Long subjectId);
        List<Grade> findBySequenceId(Long sequenceId);
        List<Grade> findByStudentIdAndClasseId(Long studentId, Long classId);
        List<Grade> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

        List<Grade> findByStudentIdAndSequenceId(Long studentId, Long sequenceId);
        List<Grade> findBySubjectIdAndSequenceId(Long subjectId, Long sequenceId);

    // 1. Recherches paginées optimisées
    Page<Grade> findByClasseIdAndPeriod(Long classId, String period, Pageable pageable);
    Page<Grade> findByStudentId(Long studentId, Pageable pageable);
    Page<Grade> findByStudentIdAndPeriod(Long studentId, String period, Pageable pageable);

    // 2. Requêtes spécifiques avec projections
    @Query("SELECT g.grade FROM Grade g WHERE g.student.id = :studentId AND g.subject.id = :subjectId AND g.sequence.id = :sequenceId")
    Optional<Double> findGradeValueByStudentSubjectAndSequence(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId,
            @Param("sequenceId") Long sequenceId);

    boolean existsByStudentIdAndSubjectIdAndSequenceId(Long studentId, Long subjectId, Long sequenceId);

    // 3. Méthode unifiée pour la recherche par période
    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId AND " +
           "(g.period = :period OR g.sequence.libelleSequence = :period)")
    Page<Grade> findByStudentIdAndPeriodFlexible(
            @Param("studentId") Long studentId,
            @Param("period") String period,
            Pageable pageable);

    // 4. Calculs statistiques optimisés
    @Query("SELECT AVG(g.grade) FROM Grade g WHERE g.student.id = :studentId")
    Optional<Double> calculateAverageByStudentId(@Param("studentId") Long studentId);

    @Query(value = """
        SELECT student_rank FROM (
            SELECT 
                student_id,
                RANK() OVER (ORDER BY AVG(grade) DESC) AS student_rank
            FROM grade
            WHERE classe_id = :classId
            GROUP BY student_id
        ) ranks
        WHERE student_id = :studentId
    """, nativeQuery = true)
    Optional<Integer> calculateStudentRank(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId);

    // 5. Recherches spécifiques avec jointures
    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.subject " +
           "WHERE g.student.id = :studentId AND g.subject.id = :subjectId")
    List<Grade> findGradesWithSubjectByStudentAndSubject(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);

    @Query("SELECT g FROM Grade g " +
           "JOIN FETCH g.sequence " +
           "WHERE g.classe.id = :classId AND g.subject.id = :subjectId")
    List<Grade> findGradesWithSequenceByClassAndSubject(
            @Param("classId") Long classId,
            @Param("subjectId") Long subjectId);
}
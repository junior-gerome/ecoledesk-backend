package com.school.platform.enrollment.infrastructure.persistence;

import com.school.platform.academic.domain.model.AnneeScolaire;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.enrollment.domain.model.InscriptionStudent;
import com.school.platform.enrollment.domain.model.Student;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

import jakarta.persistence.LockModeType;

public interface InscriptionStudentRepository extends JpaRepository<InscriptionStudent, Long> {

    interface ClassRosterRow {
        Long getInscriptionId();
        Long getStudentId();
        String getFirstNameStudent();
        String getLastNameStudent();
        Long getClassId();
        String getClassName();
        Long getSchoolYearId();
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InscriptionStudent i where i.id = :id")
    Optional<InscriptionStudent> findByIdForUpdate(@Param("id") Long id);

    Optional<InscriptionStudent> findByStudentIdAndAnneeScolaireId(Long studentId, Long anneeScolaireId);

    boolean existsByStudentIdAndAnneeScolaireId(Long studentId, Long anneeScolaireId);

    Optional<InscriptionStudent> findByStudentIdAndClasseRoomId(Long studentId, Long classeRoomId);

    List<InscriptionStudent> findByClasseRoomIdAndAnneeScolaireId(Long classeRoomId, Long anneeScolaireId);

    List<InscriptionStudent> findByAnneeScolaireId(Long anneeScolaireId);

    List<InscriptionStudent> findByClasseRoom(ClasseRoom classeRoom);

    List<InscriptionStudent> findByClasseRoomId(Long classeRoomId);
    Page<InscriptionStudent> findByClasseRoomId(Long classeRoomId, Pageable pageable);

    @Query(value = """
            SELECT
              i.id AS inscriptionId,
              s.id AS studentId,
              p.first_name AS firstNameStudent,
              p.last_name AS lastNameStudent,
              c.id AS classId,
              c.name_classe AS className,
              i.anneescolaire_id AS schoolYearId
            FROM inscription_student i
            JOIN students s ON s.id = i.student_id
            JOIN persons p ON p.id = s.person_id
            JOIN classes c ON c.id = i.classe_room_id
            WHERE i.classe_room_id = :classId
              AND (:anneeScolaireId IS NULL OR i.anneescolaire_id = :anneeScolaireId)
            ORDER BY p.last_name ASC, p.first_name ASC
            """, nativeQuery = true)
    List<ClassRosterRow> findClassRosterRowsByClass(
            @Param("classId") Long classId,
            @Param("anneeScolaireId") Long anneeScolaireId);

    long countByClasseRoom(ClasseRoom classeRoom);

    long countByStudent(Student student);

    long countByClasseRoomId(Long classeRoomId);

    long countByAnneeScolaireId(Long anneeScolaireId);
    long countByStatutPreinscription(String statutPreinscription);
    long countByStatutPreinscriptionIn(List<String> statutsPreinscription);

    long countByStudentId(Long studentId);

    long countByClasseRoomAndAnneeScolaire(ClasseRoom classeRoom, AnneeScolaire anneeScolaire);

    long countByClasseRoomIdAndAnneeScolaireId(Long classeRoomId, Long anneeScolaireId);

    Optional<InscriptionStudent> findFirstByStudentIdOrderByIdDesc(Long studentId);
}
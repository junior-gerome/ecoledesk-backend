package com.school.platform.academic.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Section;
import com.school.platform.staff.domain.model.StaffMember;


import jakarta.persistence.LockModeType;

public interface ClasseRoomRepository extends JpaRepository<ClasseRoom, Long> {
    Optional<ClasseRoom> findByNameClasse(String nameClasse);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClasseRoom c where c.id = :id")
    Optional<ClasseRoom> findByIdForUpdate(Long id);

    @Query("SELECT c  FROM ClasseRoom c LEFT JOIN FETCH c.section s LEFT JOIN FETCH c.teacher st LEFT JOIN FETCH c.academicYear a")
    List<ClasseRoom> findAllWithDetails();

    boolean existsByNameClasse(String nameClasse);

    /** Vérifie l'unicité du nom de classe dans une section donnée */
    boolean existsByNameClasseAndSectionId(String nameClasse, Long sectionId);

    /** Vérifie l'unicité du nom de classe dans une section et une année scolaire */
    boolean existsByNameClasseAndSectionIdAndAcademicYearId(String nameClasse, Long sectionId, Long academicYearId);

    boolean existsByNameClasseAndSectionIdAndAcademicYearIdAndIdNot(String nameClasse, Long sectionId, Long academicYearId, Long id);

    @Query("SELECT c FROM ClasseRoom c LEFT JOIN FETCH c.section LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.academicYear WHERE c.section.id = :sectionId")
    List<ClasseRoom> findBySectionId(Long sectionId);

    @Query("SELECT c FROM ClasseRoom c LEFT JOIN FETCH c.section LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.academicYear WHERE c.section.id = :sectionId AND c.academicYear.id = :academicYearId")
    List<ClasseRoom> findBySectionIdAndAcademicYearId(Long sectionId, Long academicYearId);

    @Query("SELECT c FROM ClasseRoom c LEFT JOIN FETCH c.section LEFT JOIN FETCH c.teacher LEFT JOIN FETCH c.academicYear WHERE c.academicYear.id = :academicYearId")
    List<ClasseRoom> findByAcademicYearId(Long academicYearId);

    List<ClasseRoom> findBySection(Section section);
}


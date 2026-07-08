package com.school.platform.academic.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Section;

import jakarta.persistence.LockModeType;

public interface ClasseRoomRepository extends JpaRepository<ClasseRoom, Long> {
    Optional<ClasseRoom> findByNameClasse(String nameClasse);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClasseRoom c where c.id = :id")
    Optional<ClasseRoom> findByIdForUpdate(Long id);

    @Query("SELECT c FROM ClasseRoom c LEFT JOIN FETCH c.teacher t LEFT JOIN FETCH c.anneeScolaire a")
    List<ClasseRoom> findAllWithDetails();

    boolean existsByNameClasse(String nameClasse);

    List<ClasseRoom> findBySectionId(Long sectionId);

    List<ClasseRoom> findBySection(Section section);
}
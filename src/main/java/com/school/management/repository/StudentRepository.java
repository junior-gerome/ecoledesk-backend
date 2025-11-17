package com.school.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
   // Page<Student> findByClasseId(Long classeRoomId, Pageable pageable);

    Page<Student> findByParentId(Long parentId, Pageable pageable);

    //@Query("SELECT COUNT(id) FROM students")


    //Page<Student> findBySectionId(Long sectionId, Pageable pageable);

    //long countByClasseId(Long classeRoomId);
    // OU Solution 2: Utilisez une requête JPQL explicite
    // @Query("SELECT COUNT(a) FROM Absence a WHERE a.student.id = :studentId")
    // int countAbsencesByStudentId(@Param("studentId") Long studentId);


    // Si tu as cette méthode custom, elle doit être définie dans une implémentation manuelle
    // int calculateAbsenceDays(Long studentId); // <-- à commenter si non implémentée
}

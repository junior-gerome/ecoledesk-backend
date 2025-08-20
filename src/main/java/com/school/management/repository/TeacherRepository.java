package com.school.management.repository;

import com.school.management.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findBySectionId(Long sectionId);
    Optional<Teacher> findByEmail(String email);
    boolean existsByEmail(String email);
}

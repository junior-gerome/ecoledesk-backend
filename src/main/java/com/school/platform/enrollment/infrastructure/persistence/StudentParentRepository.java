package com.school.platform.enrollment.infrastructure.persistence;

import com.school.platform.enrollment.domain.model.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParent, Long> {
    List<StudentParent> findByStudentId(Long studentId);
    List<StudentParent> findByParentId(Long parentId);
}

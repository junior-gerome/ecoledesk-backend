package com.school.platform.staff.infrastructure.persistence;

import com.school.platform.staff.domain.model.EmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, Long> {
    List<EmployeePosition> findByEmployeeId(Long employeeId);
    
    @Query("SELECT ep FROM EmployeePosition ep WHERE ep.employee.id = :employeeId AND ep.endDate IS NULL")
    List<EmployeePosition> findActivePositionsByEmployeeId(Long employeeId);
}

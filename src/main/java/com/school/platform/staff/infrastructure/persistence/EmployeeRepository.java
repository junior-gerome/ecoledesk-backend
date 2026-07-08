package com.school.platform.staff.infrastructure.persistence;

import com.school.platform.staff.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    boolean existsByEmployeeNumber(String employeeNumber);
    
    @Query("SELECT e FROM Employee e JOIN FETCH e.positions WHERE e.id = :id")
    Optional<Employee> findByIdWithPositions(Long id);
}

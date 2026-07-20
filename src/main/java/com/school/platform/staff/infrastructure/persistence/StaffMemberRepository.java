package com.school.platform.staff.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.school.platform.staff.domain.model.StaffMember;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    Optional<StaffMember> findByEmployeeNumberValue(String employeeNumber);
    boolean existsByEmployeeNumberValue(String employeeNumber);

    @Query("SELECT staff FROM StaffMember staff JOIN FETCH staff.person WHERE staff.id = :id")
    Optional<StaffMember> findByIdWithPerson(Long id);
}

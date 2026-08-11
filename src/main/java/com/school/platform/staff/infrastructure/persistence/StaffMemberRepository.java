package com.school.platform.staff.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.school.platform.staff.domain.model.StaffMember;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {

    Optional<StaffMember> findByEmployeeNumberValue(String employeeNumber);

    boolean existsByEmployeeNumberValue(String employeeNumber);

    @Query("SELECT s FROM StaffMember s JOIN FETCH s.person WHERE s.id = :id")
    Optional<StaffMember> findByIdWithPerson(Long id);

    @Query("SELECT s FROM StaffMember s JOIN FETCH s.person ORDER BY s.person.lastName ASC")
    List<StaffMember> findAllWithPerson();

    @Query("SELECT s FROM StaffMember s JOIN FETCH s.person WHERE s.active = true ORDER BY s.person.lastName ASC")
    List<StaffMember> findAllActiveWithPerson();
}

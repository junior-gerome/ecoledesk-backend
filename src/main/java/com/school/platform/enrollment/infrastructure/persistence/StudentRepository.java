package com.school.platform.enrollment.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.school.platform.enrollment.domain.model.Student;

import jakarta.persistence.LockModeType;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Student s where s.id = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);

    @Query(
            value = "select distinct s from Student s join s.studentParents sp where sp.parent.id = :parentId",
            countQuery = "select count(distinct s) from Student s join s.studentParents sp where sp.parent.id = :parentId")
    Page<Student> findByParentId(@Param("parentId") Long parentId, Pageable pageable);

    Page<Student> findByActiveTrue(Pageable pageable);

    List<Student> findByActiveTrue();

    @Query("""
            select s from Student s
            join s.person person
            where lower(person.lastName) = lower(:lastNameStudent)
              and lower(person.firstName) = lower(:firstNameStudent)
              and person.birthDate = :dateOfBirth
            """)
    List<Student> findByLastNameStudentIgnoreCaseAndFirstNameStudentIgnoreCaseAndDateOfBirth(
            @Param("lastNameStudent") String lastNameStudent,
            @Param("firstNameStudent") String firstNameStudent,
            @Param("dateOfBirth") LocalDate dateOfBirth);
}
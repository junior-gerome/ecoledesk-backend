package com.school.management.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.management.model.LogActivite;


@Repository
public interface LogActiviteRepository extends JpaRepository<LogActivite, Long> {
    List<LogActivite> findByUsersId(Long usersId);
    List<LogActivite> findByDateActionBetween(LocalDateTime debut, LocalDateTime fin);
    List<LogActivite> findByTableCible(String tableCible);
    List<LogActivite> findByReferenceId(Long referenceId);
}

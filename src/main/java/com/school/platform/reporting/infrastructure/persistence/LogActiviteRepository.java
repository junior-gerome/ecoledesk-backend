package com.school.platform.reporting.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.school.platform.reporting.domain.model.LogActivite;


@Repository
public interface LogActiviteRepository extends JpaRepository<LogActivite, Long> {
    List<LogActivite> findByUsersId(Long usersId);
    List<LogActivite> findByDateActionBetween(LocalDateTime debut, LocalDateTime fin);
    List<LogActivite> findByTableCible(String tableCible);
    List<LogActivite> findByReferenceId(Long referenceId);
    List<LogActivite> findAllByOrderByDateActionDesc(Pageable pageable);
}

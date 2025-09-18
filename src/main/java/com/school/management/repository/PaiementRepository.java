package com.school.management.repository;

import com.school.management.model.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByDatePaiementBetween(LocalDate startDate, LocalDate endDate);
  //  List<Paiement> findByStudentId(Long eleveId);
   // List<Paiement> findByStudentClasseId(Long classeId);
}

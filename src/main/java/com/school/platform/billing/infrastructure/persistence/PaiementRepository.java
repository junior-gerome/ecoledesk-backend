package com.school.platform.billing.infrastructure.persistence;

import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.billing.domain.model.Paiement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByDatePaiementBetween(LocalDate startDate, LocalDate endDate);
    Page<Paiement> findByDatePaiementBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    boolean existsByReceiptNumber(String receiptNumber);
    boolean existsByReceiptNumberAndIdNot(String receiptNumber, Long id);
    boolean existsByInscriptionStudentIdAndTypePaiementAndCancelledAtIsNull(Long inscriptionStudentId, TypePaiement typePaiement);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paiement p where p.id = :id")
    Optional<Paiement> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select p from Paiement p
            left join fetch p.student student
            left join fetch p.inscriptionStudent inscription
            left join fetch inscription.classeRoom classe
            left join fetch p.montant montant
            where (:studentId is null or student.id = :studentId)
              and (:type is null or p.typePaiement = :type)
              and (:startDate is null or p.datePaiement >= :startDate)
              and (:endDate is null or p.datePaiement <= :endDate)
            order by p.datePaiement desc, p.id desc
            """)
    List<Paiement> search(
            @Param("studentId") Long studentId,
            @Param("type") TypePaiement type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
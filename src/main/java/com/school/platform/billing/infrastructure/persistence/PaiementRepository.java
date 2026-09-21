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
    boolean existsByEnrollmentIdAndTypePaiementAndCancelledAtIsNull(Long enrollmentId, TypePaiement typePaiement);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paiement p where p.id = :id")
    Optional<Paiement> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select p from Paiement p
            left join fetch p.student student
            left join fetch p.enrollment enrollment
            left join fetch enrollment.classroom classroom
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

    /**
     * Version paginee de {@link #search(Long, TypePaiement, LocalDate, LocalDate)},
     * etendue avec la recherche libre (q), le numero de recu et le statut calcule
     * (CANCELLED / PAID / LATE / PENDING) directement en SQL pour rester coherent
     * avec une vraie pagination serveur. Le countQuery est separe du select
     * (les JOIN FETCH interdisent un count derive du select).
     */
    @Query(value = """
            select p from Paiement p
            left join fetch p.student student
            left join fetch student.person person
            left join fetch p.enrollment enrollment
            left join fetch enrollment.classroom classroom
            left join fetch enrollment.academicYear academicYear
            left join fetch p.montant montant
            where (:studentId is null or student.id = :studentId)
              and (:type is null or p.typePaiement = :type)
              and (:startDate is null or p.datePaiement >= :startDate)
              and (:endDate is null or p.datePaiement <= :endDate)
              and (:q is null or :q = ''
                  or lower(coalesce(person.lastName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(person.firstName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(p.receiptNumber, '')) like lower(concat('%', :q, '%')))
              and (:receiptNumber is null or :receiptNumber = ''
                  or lower(coalesce(p.receiptNumber, '')) like lower(concat('%', :receiptNumber, '%')))
              and (:status is null or :status = ''
                  or (:status = 'CANCELLED' and p.cancelledAt is not null)
                  or (:status = 'PAID' and p.cancelledAt is null and (p.montantRestant is null or p.montantRestant = 0))
                  or (:status = 'LATE' and p.cancelledAt is null and p.montantRestant > 0
                      and p.dueDate is not null and p.dueDate < current_date)
                  or (:status = 'PENDING' and p.cancelledAt is null and p.montantRestant > 0
                      and (p.dueDate is null or p.dueDate >= current_date)))
            """,
            countQuery = """
            select count(p) from Paiement p
            left join p.student student
            left join student.person person
            where (:studentId is null or student.id = :studentId)
              and (:type is null or p.typePaiement = :type)
              and (:startDate is null or p.datePaiement >= :startDate)
              and (:endDate is null or p.datePaiement <= :endDate)
              and (:q is null or :q = ''
                  or lower(coalesce(person.lastName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(person.firstName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(p.receiptNumber, '')) like lower(concat('%', :q, '%')))
              and (:receiptNumber is null or :receiptNumber = ''
                  or lower(coalesce(p.receiptNumber, '')) like lower(concat('%', :receiptNumber, '%')))
              and (:status is null or :status = ''
                  or (:status = 'CANCELLED' and p.cancelledAt is not null)
                  or (:status = 'PAID' and p.cancelledAt is null and (p.montantRestant is null or p.montantRestant = 0))
                  or (:status = 'LATE' and p.cancelledAt is null and p.montantRestant > 0
                      and p.dueDate is not null and p.dueDate < current_date)
                  or (:status = 'PENDING' and p.cancelledAt is null and p.montantRestant > 0
                      and (p.dueDate is null or p.dueDate >= current_date)))
            """)
    Page<Paiement> searchPage(
            @Param("studentId") Long studentId,
            @Param("type") TypePaiement type,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("q") String q,
            @Param("receiptNumber") String receiptNumber,
            Pageable pageable);

    /**
     * Same search as {@link #search(Long, TypePaiement, LocalDate, LocalDate)}
     * extended with the filters supported by the export endpoints.
     */
    @Query("""
            select p from Paiement p
            left join fetch p.student student
            left join fetch student.person person
            left join fetch p.enrollment enrollment
            left join fetch enrollment.classroom classroom
            left join fetch enrollment.academicYear academicYear
            left join fetch p.montant montant
            where (:studentId is null or student.id = :studentId)
              and (:classroomId is null or classroom.id = :classroomId)
              and (:academicYearId is null or academicYear.id = :academicYearId)
              and (:type is null or p.typePaiement = :type)
              and (:paymentMethod is null or p.paymentMethod = :paymentMethod)
              and (:startDate is null or p.datePaiement >= :startDate)
              and (:endDate is null or p.datePaiement <= :endDate)
              and (:q is null or :q = ''
                  or lower(coalesce(person.lastName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(person.firstName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(p.receiptNumber, '')) like lower(concat('%', :q, '%')))
              and (:receiptNumber is null or :receiptNumber = ''
                  or lower(coalesce(p.receiptNumber, '')) like lower(concat('%', :receiptNumber, '%')))
            order by p.datePaiement desc, p.id desc
            """)
    List<Paiement> searchExport(
            @Param("studentId") Long studentId,
            @Param("classroomId") Long classroomId,
            @Param("academicYearId") Long academicYearId,
            @Param("type") TypePaiement type,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("q") String q,
            @Param("receiptNumber") String receiptNumber);
}
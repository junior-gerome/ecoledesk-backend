package com.school.platform.billing.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.billing.domain.model.Montant;

@Repository
public interface MontantRepository extends JpaRepository<Montant, Long> {
  
  Optional<Montant> findByClasseRoomId(Long classeRoomId);

  Optional<Montant> findByClasseRoomIdAndTypePaiement(Long classeRoomId , TypePaiement typePaiement);

  Optional<Montant> findFirstByClasseRoomIdOrderByIdDesc(Long classeRoomId);

}

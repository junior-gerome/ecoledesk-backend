package com.school.management.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.management.model.ClasseRoom;
import com.school.management.enums.TypePaiement;
import com.school.management.model.Montant;

@Repository
public interface MontantRepository extends JpaRepository<Montant, Long> {
  
  Optional<Montant> findByClasseRoomId(Long classeRoomId);

  Optional<Montant> findByClasseRoomIdAndTypePaiement(Long classeRoomId , TypePaiement typePaiement);


}

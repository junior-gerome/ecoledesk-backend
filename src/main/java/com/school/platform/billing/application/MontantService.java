package com.school.platform.billing.application;

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.school.platform.billing.infrastructure.persistence.MontantRepository;
import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.billing.domain.model.Montant;
import com.school.platform.billing.domain.model.TypePaiement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MontantService {
  private final MontantRepository montantRepository;


  public Montant createMontant(Montant montant) {
    return montantRepository.save(montant);
  }

  public Montant getMontantById(Long id) {
    return montantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Montant", "id", id));
  }

  // public Montant updateMontant(Long id, Montant montant) {
  //   Montant exisMontant = getMontantById(id);

  //   if (!exisMontant.getCount().equals(montant.getCount())) {
  //     throw new IllegalArgumentException("le montant avec ce count" + montant.getCount() + "existe deja.");

  //   }

  //   return montantService.save();
  // }

  public List<Montant> getAllMontant() {
    return montantRepository.findAll();
  }
  
  public Montant getByClasseAndType(Long classeRoomId, String typePaiement){
    TypePaiement type;
    try {
      type = TypePaiement.valueOf(typePaiement.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Type de paiement invalide: " + typePaiement);
    }

    Optional<Montant> montant = montantRepository.findByClasseRoomIdAndTypePaiement(classeRoomId, type);
    if (montant.isPresent()) {
      return montant.get();
    }

    if (type == TypePaiement.FRAIS_PREINSCRIPTION) {
      return montantRepository.findByClasseRoomIdAndTypePaiement(classeRoomId, TypePaiement.FRAIS_INSCRIPTION)
          .orElseThrow(() -> new ResourceNotFoundException("Montant", "classeRoomId and typePaiement", classeRoomId + " and " + typePaiement));
    }

    return montant.orElseThrow(() -> new ResourceNotFoundException("Montant", "classeRoomId and typePaiement", classeRoomId + " and " + typePaiement) );
  }

  public Montant getPreinscriptionByClasse(Long classeRoomId) {
    return montantRepository.findByClasseRoomIdAndTypePaiement(classeRoomId, TypePaiement.FRAIS_PREINSCRIPTION)
        .or(() -> montantRepository.findByClasseRoomIdAndTypePaiement(classeRoomId, TypePaiement.FRAIS_INSCRIPTION))
        .or(() -> montantRepository.findFirstByClasseRoomIdOrderByIdDesc(classeRoomId))
        .orElseThrow(() -> new ResourceNotFoundException("Montant", "classeRoomId", classeRoomId));
  }

  public Montant getByClasse(Long classeRoomId){
    return montantRepository.findByClasseRoomId(classeRoomId).orElseThrow(() -> new ResourceNotFoundException("Montant", "classeRoomId", classeRoomId) );
  }

  public Montant updateMontant(Long id, Montant montant) {
    Montant existing = getMontantById(id);
    existing.setCount(montant.getCount());
    existing.setClasseRoom(montant.getClasseRoom());
    existing.setTypePaiement(montant.getTypePaiement());
    return montantRepository.save(existing);
  }

  public void deleteMontant(Long id) {
    if (!montantRepository.existsById(id)) {
      throw new ResourceNotFoundException("Montant", "id", id);
    }
    montantRepository.deleteById(id);
  }

  public Montant saveOrUpdate(Montant montant) {
    Long classeRoomId = montant.getClasseRoom().getId();
    TypePaiement typePaiement = montant.getTypePaiement();
    Optional<Montant> existingMontant = montantRepository.findByClasseRoomIdAndTypePaiement(classeRoomId, typePaiement);
    if (existingMontant.isPresent()) {
      Montant montantToUpdate = existingMontant.get();
      montantToUpdate.setCount(montant.getCount());
      return montantRepository.save(montantToUpdate);
    }else {
    return montantRepository.save(montant);
  }
}

}

package com.school.management.service;

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.school.management.repository.MontantRepository;
import com.school.exception.BadRequestException;
import com.school.exception.ResourceNotFoundException;
import com.school.management.model.Montant;
import com.school.management.enums.TypePaiement;
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
    return montantRepository.findByClasseRoomIdAndTypePaiement(classeRoomId, type)
        .orElseThrow(() -> new ResourceNotFoundException("Montant", "classeRoomId and typePaiement", classeRoomId + " and " + typePaiement) );
  }

  public Montant getByClasse(Long classeRoomId){
    return montantRepository.findByClasseRoomId(classeRoomId).orElseThrow(() -> new ResourceNotFoundException("Montant", "classeRoomId", classeRoomId) );
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

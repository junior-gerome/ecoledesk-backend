package com.school.management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.exception.ResourceNotFoundException;
import com.school.management.dto.AnneeScolaireDTO;
import com.school.management.mappers.AnneeScolaireMapper;
import com.school.management.model.AnneeScolaire;
import com.school.management.repository.AnneeScolaireRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnneeScolaireService {
  private final AnneeScolaireRepository anneeScolaireRepository;
  private final AnneeScolaireMapper mapper;

   
    public List<AnneeScolaireDTO> getAllAnneeScolaire() {
      
      return anneeScolaireRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }
    
    public AnneeScolaireDTO createAnneeScolaire(AnneeScolaireDTO dto) {
      AnneeScolaire entity = mapper.toEntity(dto);
      AnneeScolaire saved = anneeScolaireRepository.save(entity);
      return mapper.toDto(saved);
    }

    @Transactional
    public AnneeScolaireDTO getById(Long id) {
      return anneeScolaireRepository.findById(id).map(mapper::toDto).orElseThrow(()-> new ResourceNotFoundException("AnneeScolaire", "id", id));
    }

    public AnneeScolaireDTO updateAnneeScolaire(Long id, AnneeScolaireDTO dto) {
      AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'ID : " + id));

      //AnneeScolaire entity = mapper.toEntity(dto);

      //anneeScolaire.setId(dto.getId());
      anneeScolaire.setLibelleAnneeScolaire(dto.getLibelleAnneeScolaire());
      anneeScolaire.setDateDebut(dto.getDateDebut());
      anneeScolaire.setDateFin(dto.getDateFin());
      anneeScolaire.setStatutCode(dto.isStatutCode());
      AnneeScolaire updated = anneeScolaireRepository.save(anneeScolaire);

      return mapper.toDto(updated);
    }
    
    public void deleteAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'ID : " + id));
      anneeScolaireRepository.delete(anneeScolaire);
    } 

    public AnneeScolaireDTO getActiveAnneeScolaire() {
      return anneeScolaireRepository.findByStatutCode(true).map(mapper::toDto)
          .orElseThrow(() -> new RuntimeException("Aucune année scolaire active trouvée"));
    }
    
    public AnneeScolaire activateAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaireToActivate = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'ID : " + id));

      // Désactiver l'année scolaire actuellement active
      anneeScolaireRepository.findByStatutCode(true).ifPresent(activeAnnee -> {
        activeAnnee.setStatutCode(false);
        anneeScolaireRepository.save(activeAnnee);
      });

      // Activer la nouvelle année scolaire
      anneeScolaireToActivate.setStatutCode(true);
      return anneeScolaireRepository.save(anneeScolaireToActivate);
    }
    
    public boolean existsByStatutCode(boolean statutCode) {
      return anneeScolaireRepository.findByStatutCode(statutCode).isPresent();
    }

    public AnneeScolaire inactivateAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaireToInactivate = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'ID : " + id));

      // Inactiver l'année scolaire spécifiée
      anneeScolaireToInactivate.setStatutCode(false);
      return anneeScolaireRepository.save(anneeScolaireToInactivate);
    } 

}

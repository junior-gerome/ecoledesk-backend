package com.school.management.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.school.management.model.AnneeScolaire;
import com.school.management.repository.AnneeScolaireRepository;

@Service
public class AnneeScolaireService {
    private final AnneeScolaireRepository anneeScolaireRepository;

    public AnneeScolaireService(AnneeScolaireRepository anneeScolaireRepository) {
      this.anneeScolaireRepository = anneeScolaireRepository;
    }
    
    public List<AnneeScolaire> getAllAnneeScolaire() {
      return anneeScolaireRepository.findAll();
    }
    
    public AnneeScolaire createAnneeScolaire(AnneeScolaire anneeScolaire) {
      return anneeScolaireRepository.save(anneeScolaire);
    }

    public AnneeScolaire updateAnneeScolaire(Long id, AnneeScolaire anneeScolaireDetails) {
      AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'ID : " + id));

      anneeScolaire.setLibelleAnneeScolaire(anneeScolaireDetails.getLibelleAnneeScolaire());
      anneeScolaire.setDateDebut(anneeScolaireDetails.getDateDebut());
      anneeScolaire.setDateFin(anneeScolaireDetails.getDateFin());
      anneeScolaire.setStatutCode(anneeScolaireDetails.isStatutCode());

      return anneeScolaireRepository.save(anneeScolaire);
    }
    
    public void deleteAnneeScolaire(Long id) {
      AnneeScolaire anneeScolaire = anneeScolaireRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Année scolaire non trouvée avec l'ID : " + id));
      anneeScolaireRepository.delete(anneeScolaire);
    } 

    public AnneeScolaire getActiveAnneeScolaire() {
      return anneeScolaireRepository.findByStatutCode(true)
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

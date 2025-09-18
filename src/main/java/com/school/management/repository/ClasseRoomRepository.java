package com.school.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.management.model.ClasseRoom;


public interface ClasseRoomRepository extends JpaRepository<ClasseRoom, Long> {
  // Recherche par nom de la classe
    Optional<ClasseRoom> findByNameClasse(String nameClasse);

    // Vérifie l’existence d’une classe par son nom
    boolean existsByNameClasse(String nameClasse);

    // Récupère toutes les classes d’une section donnée
    List<ClasseRoom> findBySectionId(Long sectionId);

    
}

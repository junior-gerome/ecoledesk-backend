package com.school.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.school.management.model.ClasseRoom;
import com.school.management.model.Section;


public interface ClasseRoomRepository extends JpaRepository<ClasseRoom, Long> {
  // Recherche par nom de la classe
  Optional<ClasseRoom> findByNameClasse(String nameClasse);

  // Utilisation d'un Fetch Join pour charger Teacher et AnneeScolaire en une seule requête SQL
    @Query("SELECT c FROM ClasseRoom c LEFT JOIN FETCH c.teacher t LEFT JOIN FETCH c.anneeScolaire a")
    List<ClasseRoom> findAllWithDetails();
    

    // Vérifie l’existence d’une classe par son nom
    boolean existsByNameClasse(String nameClasse);

    // Récupère toutes les classes d’une section donnée
    List<ClasseRoom> findBySectionId(Long sectionId);

    List<ClasseRoom> findBySection(Section section);


    
}

package com.school.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.management.model.Classe;


public interface ClasseRepository extends JpaRepository<Classe, Long> {
  Optional<Classe> findByNameClasse(String nameClasse);
    boolean existsByNameClasse(String nameClasse);
}

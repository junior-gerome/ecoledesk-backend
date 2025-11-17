package com.school.management.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AnneeScolaireDTO {
  private Long id;
  private String libelleAnneeScolaire;
  private LocalDate dateDebut;
  private LocalDate dateFin;
  private boolean statutCode;

}

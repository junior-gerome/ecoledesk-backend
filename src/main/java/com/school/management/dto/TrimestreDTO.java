package com.school.management.dto;


import lombok.Data;

@Data
public class TrimestreDTO{
  private Long id;
  private String libelleTrimestre;
  private AnneeScolaireDTO anneeScolaire;
}
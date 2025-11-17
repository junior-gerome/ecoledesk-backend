package com.school.management.dto;

import lombok.Data;

@Data
public class SequenceDTO{
  private Long id;
  private String libelleSequence;
  private TrimestreDTO trimestre;

}
package com.school.platform.academic.application.dto;

import lombok.Data;

@Data
public class SequenceDTO{
  private Long id;
  private String libelleSequence;
  private TrimestreDTO trimestre;

}
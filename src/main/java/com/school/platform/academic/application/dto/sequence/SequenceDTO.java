package com.school.platform.academic.application.dto.sequence;

import com.school.platform.academic.application.dto.trimestre.TrimestreDTO;
import lombok.Data;

@Data
public class SequenceDTO{
  private Long id;
  private String libelleSequence;
  private TrimestreDTO trimestre;

}
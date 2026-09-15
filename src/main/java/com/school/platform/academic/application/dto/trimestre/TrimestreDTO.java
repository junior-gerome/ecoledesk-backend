package com.school.platform.academic.application.dto.trimestre;


import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TrimestreDTO{
  private Long id;
  private String libelleTrimestre;
  @JsonProperty("anneeScolaire")
  private AcademicYearDTO academicYear;
}
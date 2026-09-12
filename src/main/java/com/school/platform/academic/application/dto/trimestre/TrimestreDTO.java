package com.school.platform.academic.application.dto.trimestre;


import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAlias;

@Data
public class TrimestreDTO{
  private Long id;
  private String libelleTrimestre;
  @JsonAlias("anneeScolaire")
  private AcademicYearDTO academicYear;
}
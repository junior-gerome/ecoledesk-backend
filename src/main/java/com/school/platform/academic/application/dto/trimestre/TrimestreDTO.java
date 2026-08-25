package com.school.platform.academic.application.dto.trimestre;


import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import lombok.Data;

@Data
public class TrimestreDTO{
  private Long id;
  private String libelleTrimestre;
  private AcademicYearDTO academicYear;
}
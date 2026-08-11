package com.school.platform.academic.application.dto;


import lombok.Data;

@Data
public class TrimestreDTO{
  private Long id;
  private String libelleTrimestre;
  private AcademicYearDTO academicYear;
}
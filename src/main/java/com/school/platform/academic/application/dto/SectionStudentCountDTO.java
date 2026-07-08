package com.school.platform.academic.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SectionStudentCountDTO  {
  private String sectionName;
  private Long studentCount;
  private Long newStudentCount;

  public SectionStudentCountDTO(String sectionName, Long studentCount) {
    this(sectionName, studentCount, 0L);
  }

  public SectionStudentCountDTO(String sectionName, Long studentCount, Long newStudentCount) {
    this.sectionName = sectionName;
    this.studentCount = studentCount;
    this.newStudentCount = newStudentCount;
  }
}

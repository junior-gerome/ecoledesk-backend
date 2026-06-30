package com.school.platform.academic.application.dto;

import lombok.Data;

@Data
public class SubjectDTO {
  private Long id;
  private String nameSubject;
  private String code;
  private Integer coefficient;
  private String description;
}

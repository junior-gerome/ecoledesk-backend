package com.school.platform.academic.application.dto.subject;

import lombok.Data;

@Data
public class SubjectRequest {

  private String nameSubject;
  private String code;
  private Integer coefficient;
  private String description;
}

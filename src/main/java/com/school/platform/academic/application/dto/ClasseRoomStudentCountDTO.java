package com.school.platform.academic.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClasseRoomStudentCountDTO {
  private String nameClasseRoom;
  private Long studentCount;
  private Long newStudentCount;

  public ClasseRoomStudentCountDTO(String nameClasseRoom, Long studentCount) {
    this(nameClasseRoom, studentCount, 0L);
  }

  public ClasseRoomStudentCountDTO(String nameClasseRoom, Long studentCount, Long newStudentCount) {
    this.nameClasseRoom = nameClasseRoom;
    this.studentCount = studentCount;
    this.newStudentCount = newStudentCount;
  }
}

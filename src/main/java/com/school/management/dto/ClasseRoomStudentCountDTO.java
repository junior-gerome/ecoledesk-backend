package com.school.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasseRoomStudentCountDTO {
  private String nameClasseRoom;
  private Long studentCount;

}

package com.school.management.dto;
 import lombok.Data; 
 import java.time.LocalDate;
 import com.school.management.enums.Gender;
 
  
@Data 
public class StudentDTO { 
  private Long id;
  private String firstNameStudent;
  private String lastNameStudent;
  private LocalDate dateOfBirth;
  private Gender gender;
  private String ecolePrecedente;
  private ParentDTO parent;
            
}
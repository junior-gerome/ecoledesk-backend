package com.school.management.dto;
 import lombok.Data; 
 import lombok.Builder; 
 import java.time.LocalDate;
 import java.time.LocalDateTime; 
 import com.school.management.enums.Gender;
 import com.school.management.model.*;
  
   //import com.google.auto.value.AutoValue.Builder;
@Builder 
@Data 
public class StudentDTO { 
  private Long id; 
  private String lastNameStudent ;
  private String firstNameStudent ;
  private LocalDate dateOfBirth;
  private LocalDateTime registrationDate;
  private Gender gender;
  //private Boolean active = true;
  private ClasseRoom classe; 
  private Section section; 
  private String ecolePrecedente; 
            
}
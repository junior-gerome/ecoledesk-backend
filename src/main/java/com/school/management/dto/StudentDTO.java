package com.school.management.dto;
 import lombok.Data; 
// import lombok.Builder; 
 import java.time.LocalDate;
// import java.time.LocalDateTime; 
 import com.school.management.enums.Gender;
 
  
   //import com.google.auto.value.AutoValue.Builder;

@Data 
public class StudentDTO { 
  private Long id;
    private String firstNameStudent;
    private String lastNameStudent;
    //private String fullNameStudent;
    private LocalDate dateOfBirth;
    private Gender gender;
   private Boolean active;
    private String ecolePrecedente;
    private Long parentId; 
            
}
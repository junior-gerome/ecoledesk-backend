package com.school.management.dto;

import lombok.Data;

@Data
public class StudentRequest {
   private String lastNameStudent;
    private String firstNameStudent;
    private String dateOfBirth;
    private String gender;
    private ParentRequest parent;
}

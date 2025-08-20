package com.school.management.dto;

import com.school.management.model.Section;
import com.school.management.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TeacherDTO {
    private Long id;
    private String lastnameTeacher;
    private String firstnameTeacher;
    private String email;
    private String phoneNumber;
    private Section section;
    private Gender gender;
    private String niveau;
    private String speciality;

    public TeacherDTO(){}
   
}

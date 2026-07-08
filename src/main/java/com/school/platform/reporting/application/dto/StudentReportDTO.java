package com.school.platform.reporting.application.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StudentReportDTO {
    private Long Id;

    private String NameClasse;

    private String firstnameteacher;
    private String lastnameTeacher;
    
    private String lastNameStudent ;

    private String firstNameStudent ;

    private String LibelleanneeSection;

    private String period;

    private LocalDate reportDate;

    private List<SubjectGradeDTO> grades;

    
    private Double averageGrade;

  
    private Double generalAverage;

    @Min(value = 1, message = "Le rang doit être supérieur à 0")
    private Integer rank;

    @Min(value = 1, message = "Le nombre total d'élèves doit être supérieur à 0")
    private Integer totalStudents;

    @Min(value = 0, message = "Le nombre de jours d'absence ne peut pas être négatif")
    private Integer absenceDays;

    private String teacherComments;
    private String attendance;
}

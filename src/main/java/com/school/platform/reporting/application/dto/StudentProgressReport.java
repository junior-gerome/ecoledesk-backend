package com.school.platform.reporting.application.dto;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.school.platform.academic.application.dto.GradeDTO;
import com.school.platform.enrollment.application.dto.StudentDTO;

import lombok.Data;

@Data
public class StudentProgressReport {
    private StudentDTO student;
    private String classLevel;
    private String LibelleanneeSection;
    private Double averageGrade = 0.0;
    private Map<String, Double> gradesBySubject = new HashMap<>();
    private Integer rank = 0;
    private Integer totalStudents = 0;
    private Integer absenceDays = 0;
    private String LastnameTeacher;
    private String period;
    private List<GradeDTO> gradeHistory;
    private Map<String, List<Double>> progressBySubject = new HashMap<>();
    private Long classId;
}

package com.school.platform.reporting.application.dto;

import java.util.Map;
import java.util.HashMap;

import lombok.Data;

@Data
public class ClassPerformanceReport {
    private Long classId;
    private String NameClasse;
    private String period;
    private Map<String, Double> subjectAverages = new HashMap<>();
    private Double classAverage = 0.0;
    private Double successRate = 0.0;
    private Integer totalStudents = 0;
    private Integer passingStudents = 0;
    private Map<String, Integer> gradeDistribution = new HashMap<>();
    private Double highestAverage = 0.0;
    private Double lowestAverage = 0.0;

    public double getClassSuccessRate() {
        return successRate;
    }

    public void setClassSuccessRate(double classSuccessRate) {
        this.successRate = classSuccessRate;
    }
}

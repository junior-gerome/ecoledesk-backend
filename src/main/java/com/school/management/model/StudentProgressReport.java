package com.school.management.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "student_progress_report")
public class StudentProgressReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "period", nullable = false, length = 20)
    private String period;

    @Column(name = "average_grade")
    private Double averageGrade;

    @Column(name = "attendance_rate")
    private Double attendanceRate;

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public String getStudentName() {
        return student != null ? student.getLastNameStudent()+ "" + student.getFirstNameStudent(): "";
    }
} 
package com.school.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PredictiveAnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public PredictiveAnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable("predictiveAnalytics")
    public Map<String, Object> predictStudentPerformance(Long studentId) {
        Map<String, Object> predictions = new HashMap<>();
        predictions.put("predictedGrades", predictGrades(studentId));
        predictions.put("failureRisk", calculateFailureRisk(studentId));
        predictions.put("progressionTrend", calculateProgressionTrend(studentId));
        return predictions;
    }

    private Map<String, Double> predictGrades(Long studentId) {
        String sql = """
            WITH RecentGrades AS (
                SELECT s.id AS subject_id, 
                       s.name AS subject,
                       g.grade,
                       ROW_NUMBER() OVER (PARTITION BY s.id ORDER BY seq.number DESC) AS rn
                FROM grade g
                JOIN subject s ON g.subject_id = s.id
                JOIN sequence seq ON g.sequence_id = seq.id
                WHERE g.student_id = ?
            )
            SELECT subject,
                   AVG(grade) AS current_avg,
                   CASE 
                       WHEN COUNT(*) >= 3 THEN 
                           AVG(grade) + (MAX(grade) - MIN(grade)) * 0.1
                       ELSE AVG(grade)
                   END AS predicted_grade
            FROM RecentGrades
            WHERE rn <= 3  -- Dernières 3 notes par matière
            GROUP BY subject_id, subject
        """;
        
        Map<String, Double> predictions = new HashMap<>();
        jdbcTemplate.query(sql, ps -> ps.setLong(1, studentId), rs -> {
            predictions.put(rs.getString("subject"), rs.getDouble("predicted_grade"));
        });
        return predictions;
    }

    private Map<String, Double> calculateFailureRisk(Long studentId) {
        String sql = """
            WITH RecentGrades AS (
                SELECT s.id AS subject_id,
                       s.name AS subject,
                       AVG(g.grade) AS avg_grade,
                       COUNT(g.id) AS grade_count,
                       MIN(g.grade) AS min_grade
                FROM grade g
                JOIN subject s ON g.subject_id = s.id
                JOIN sequence seq ON g.sequence_id = seq.id
                WHERE g.student_id = ?
                  AND seq.number >= (SELECT MAX(number) - 2 FROM sequence)
                GROUP BY s.id, s.name
            )
            SELECT subject,
                   CASE 
                       WHEN avg_grade < 10 THEN 0.8
                       WHEN avg_grade < 12 THEN 0.5
                       WHEN avg_grade < 14 THEN 0.3
                       ELSE 0.1
                   END * CASE
                       WHEN grade_count < 3 THEN 1.2
                       ELSE 1.0
                   END * CASE
                       WHEN min_grade < 8 THEN 1.3
                       ELSE 1.0
                   END AS risk
            FROM RecentGrades
        """;
        
        Map<String, Double> risks = new HashMap<>();
        jdbcTemplate.query(sql, ps -> ps.setLong(1, studentId), rs -> {
            risks.put(rs.getString("subject"), rs.getDouble("risk"));
        });
        return risks;
    }

    private Map<String, Object> calculateProgressionTrend(Long studentId) {  // Paramètre corrigé (studentId au lieu de subjectId)
        String sql = """
            WITH SequenceProgress AS (
                SELECT seq.number AS sequence_num,
                       s.name AS subject,
                       AVG(g.grade) AS avg_grade
                FROM grade g
                JOIN subject s ON g.subject_id = s.id
                JOIN sequence seq ON g.sequence_id = seq.id
                WHERE g.student_id = ?
                GROUP BY seq.number, s.name
                ORDER BY seq.number
            )
            SELECT subject,
                   REGR_SLOPE(avg_grade, sequence_num) AS trend,
                   AVG(avg_grade) AS overall_avg
            FROM SequenceProgress
            GROUP BY subject
        """;
        
        Map<String, Object> trends = new HashMap<>();
        jdbcTemplate.query(sql, ps -> ps.setLong(1, studentId), rs -> {
            Map<String, Double> subjectData = new HashMap<>();
            subjectData.put("trend", rs.getDouble("trend"));
            subjectData.put("overallAvg", rs.getDouble("overall_avg"));
            trends.put(rs.getString("subject"), subjectData);
        });
        return trends;
    }

    @Cacheable("predictiveEnrollment")
    public Map<String, Object> predictEnrollmentTrends() {
        String sql = """
            WITH EnrollmentHistory AS (
                SELECT 
                    sec.id AS section_id,
                    sec.name AS section,
                    COUNT(e.id) AS enrollment_count,
                    YEAR(e.registration_date) AS year
                FROM student e
                JOIN section sec ON e.section_id = sec.id
                GROUP BY sec.id, sec.name, YEAR(e.registration_date)
            )
            SELECT 
                section,
                enrollment_count,
                year,
                AVG(enrollment_count) OVER (
                    PARTITION BY section ORDER BY year 
                    ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
                ) AS moving_avg,
                REGR_SLOPE(enrollment_count, year) OVER (PARTITION BY section) AS trend
            FROM EnrollmentHistory
            ORDER BY section, year
        """;
        
        return jdbcTemplate.queryForMap(sql);
    }
}
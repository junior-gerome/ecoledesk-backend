package com.school.platform.reporting.application;

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
                       s.name_subject AS subject,
                       g.grade,
                       ROW_NUMBER() OVER (PARTITION BY s.id ORDER BY seq.id DESC) AS rn
                FROM academic_grade g
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
                       s.name_subject AS subject,
                       AVG(g.grade) AS avg_grade,
                       COUNT(g.id) AS grade_count,
                       MIN(g.grade) AS min_grade
                FROM academic_grade g
                JOIN subject s ON g.subject_id = s.id
                JOIN sequence seq ON g.sequence_id = seq.id
                WHERE g.student_id = ?
                  AND seq.id >= (SELECT MAX(id) - 2 FROM sequence)
                GROUP BY s.id, s.name_subject
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
                SELECT seq.id AS sequence_num,
                       s.name_subject AS subject,
                       AVG(g.grade) AS avg_grade
                FROM academic_grade g
                JOIN subject s ON g.subject_id = s.id
                JOIN sequence seq ON g.sequence_id = seq.id
                WHERE g.student_id = ?
                GROUP BY seq.id, s.name_subject
            )
            SELECT subject,
                   CASE 
                       WHEN COUNT(*) > 1 AND (COUNT(*) * SUM(sequence_num * sequence_num) - SUM(sequence_num) * SUM(sequence_num)) <> 0 THEN
                           (COUNT(*) * SUM(sequence_num * avg_grade) - SUM(sequence_num) * SUM(avg_grade)) / 
                           (COUNT(*) * SUM(sequence_num * sequence_num) - SUM(sequence_num) * SUM(sequence_num))
                       ELSE 0.0
                   END AS trend,
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
    public java.util.List<Map<String, Object>> predictEnrollmentTrends() {
        String sql = """
            WITH EnrollmentHistory AS (
                SELECT 
                    sec.id AS section_id,
                    sec.libelle AS section,
                    COUNT(DISTINCT i.student_id) AS enrollment_count,
                    YEAR(i.date_inscription) AS year
                FROM inscription_student i
                JOIN classes c ON i.classe_room_id = c.id
                JOIN sections sec ON c.section_id = sec.id
                GROUP BY sec.id, sec.libelle, YEAR(i.date_inscription)
            ),
            SectionTrend AS (
                SELECT 
                    section_id,
                    CASE 
                        WHEN COUNT(*) > 1 AND (COUNT(*) * SUM(year * year) - SUM(year) * SUM(year)) <> 0 THEN
                            (COUNT(*) * SUM(year * enrollment_count) - SUM(year) * SUM(enrollment_count)) / 
                            (COUNT(*) * SUM(year * year) - SUM(year) * SUM(year))
                        ELSE 0.0
                    END AS trend
                FROM EnrollmentHistory
                GROUP BY section_id
            )
            SELECT 
                h.section,
                h.enrollment_count,
                h.year,
                AVG(h.enrollment_count) OVER (
                    PARTITION BY h.section ORDER BY h.year 
                    ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
                ) AS moving_avg,
                t.trend AS trend
            FROM EnrollmentHistory h
            JOIN SectionTrend t ON h.section_id = t.section_id
            ORDER BY h.section, h.year
        """;
        
        return jdbcTemplate.queryForList(sql);
    }
}
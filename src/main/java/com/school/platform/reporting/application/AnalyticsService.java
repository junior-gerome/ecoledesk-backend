package com.school.platform.reporting.application;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable("statisticsCache")
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Effectif total par section
        stats.put("effectifParSection", getEffectifParSection());
        
        // Moyenne générale par classe
        stats.put("moyenneParClasse", getMoyenneParClasse());
        
        // Taux de réussite par matière
        stats.put("tauxReussiteParMatiere", getTauxReussiteParMatiere());
        
        // Statistiques de paiement
        stats.put("statistiquesPaiement", getStatistiquesPaiement());
        
        return stats;
    }

    private List<Map<String, Object>> getEffectifParSection() {
        String sql = """
            SELECT s.libelle as section, COUNT(DISTINCT e.student_id) as effectif
            FROM sections s
            LEFT JOIN classes c ON c.section_id = s.id
            LEFT JOIN enrollments e ON e.classroom_id = c.id AND e.status = 'CONFIRMED'
            GROUP BY s.id, s.libelle
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getMoyenneParClasse() {
        String sql = """
            SELECT c.name_classe as classe, 
                   sub.name_subject as matiere,
                   ROUND(AVG(g.grade), 2) as moyenne
            FROM academic_grade g
            JOIN classes c ON g.classe_id = c.id
            JOIN subject sub ON g.subject_id = sub.id
            GROUP BY c.id, c.name_classe, sub.id, sub.name_subject
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getTauxReussiteParMatiere() {
        String sql = """
            SELECT sub.name_subject as matiere,
                   ROUND(COUNT(CASE WHEN g.grade >= 10 THEN 1 END) * 100.0 / COUNT(*), 2) as taux_reussite
            FROM academic_grade g
            JOIN subject sub ON g.subject_id = sub.id
            GROUP BY sub.id, sub.name_subject
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getStatistiquesPaiement() {
        String sql = """
            SELECT p.type_paiement as type_paiement,
                   COUNT(p.id) as nombre_paiements,
                   SUM(p.montant_paye) as montant_total
            FROM paiements p
            GROUP BY p.type_paiement
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @Cacheable("performanceCache")
    public Map<String, Object> getPerformanceIndicators() {
        Map<String, Object> indicators = new HashMap<>();
        
        // Évolution des moyennes
        indicators.put("evolutionMoyennes", getEvolutionMoyennes());
        
        // Assiduité des élèves
        indicators.put("tauxPresence", getTauxPresence());
        
        // Progression des paiements
        indicators.put("progressionPaiements", getProgressionPaiements());
        
        return indicators;
    }

    private List<Map<String, Object>> getEvolutionMoyennes() {
        String sql = """
            SELECT c.name_classe as classe,
                   t.libelle as trimestre,
                   ROUND(AVG(g.grade), 2) as moyenne
            FROM academic_grade g
            JOIN classes c ON g.classe_id = c.id
            JOIN trimestre t ON g.trimestre_id = t.id
            GROUP BY c.id, c.name_classe, t.id, t.libelle, t.numero
            ORDER BY t.numero
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getTauxPresence() {
        String sql = """
            SELECT c.name_classe as classe,
                   ROUND(COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) * 100.0 / COUNT(*), 2) as taux_presence
            FROM absences a
            JOIN students s ON a.student_id = s.id
            JOIN enrollments e ON e.student_id = s.id AND e.status = 'CONFIRMED'
            JOIN classes c ON e.classroom_id = c.id
            GROUP BY c.id, c.name_classe
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getProgressionPaiements() {
        String sql = """
            SELECT DATE_FORMAT(p.date_paiement, '%Y-%m') as mois,
                   COUNT(p.id) as nombre_paiements,
                   SUM(p.montant_paye) as montant_total
            FROM paiements p
            GROUP BY DATE_FORMAT(p.date_paiement, '%Y-%m')
            ORDER BY mois
        """;
        return jdbcTemplate.queryForList(sql);
    }
}

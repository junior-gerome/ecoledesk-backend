package com.school.service;

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
            SELECT s.nom as section, COUNT(e.id) as effectif
            FROM section s
            LEFT JOIN eleve e ON e.section_id = s.id
            GROUP BY s.id, s.nom
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getMoyenneParClasse() {
        String sql = """
            SELECT c.nom as classe, 
                   m.nom as matiere,
                   ROUND(AVG(n.note), 2) as moyenne
            FROM note n
            JOIN eleve e ON n.eleve_id = e.id
            JOIN classe c ON e.classe_id = c.id
            JOIN matiere m ON n.matiere_id = m.id
            GROUP BY c.id, c.nom, m.id, m.nom
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getTauxReussiteParMatiere() {
        String sql = """
            SELECT m.nom as matiere,
                   COUNT(CASE WHEN n.note >= 10 THEN 1 END) * 100.0 / COUNT(*) as taux_reussite
            FROM note n
            JOIN matiere m ON n.matiere_id = m.id
            GROUP BY m.id, m.nom
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getStatistiquesPaiement() {
        String sql = """
            SELECT tp.libelle as type_paiement,
                   COUNT(p.id) as nombre_paiements,
                   SUM(p.montant) as montant_total
            FROM paiement p
            JOIN type_paiement tp ON p.type_paiement_id = tp.id
            GROUP BY tp.id, tp.libelle
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
            SELECT c.nom as classe,
                   t.libelle as trimestre,
                   ROUND(AVG(n.note), 2) as moyenne
            FROM note n
            JOIN eleve e ON n.eleve_id = e.id
            JOIN classe c ON e.classe_id = c.id
            JOIN trimestre t ON n.trimestre_id = t.id
            GROUP BY c.id, c.nom, t.id, t.libelle
            ORDER BY t.numero
        """;
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> getTauxPresence() {
        // À implémenter quand la table des présences sera ajoutée
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getProgressionPaiements() {
        String sql = """
            SELECT DATE_FORMAT(p.date_paiement, '%Y-%m') as mois,
                   COUNT(p.id) as nombre_paiements,
                   SUM(p.montant) as montant_total
            FROM paiement p
            GROUP BY DATE_FORMAT(p.date_paiement, '%Y-%m')
            ORDER BY mois
        """;
        return jdbcTemplate.queryForList(sql);
    }
}

package com.school.platform.notification.infrastructure.push;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@Service
public class PushNotificationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Enlève tout ce qui est Firebase lié : on garde juste la récupération des tokens

    public void sendNotificationToParent(Long parentId, String title, String body) {
        String sql = "SELECT token FROM parent_devices WHERE parent_id = ?";
        List<String> tokens = jdbcTemplate.queryForList(sql, String.class, parentId);
        
        // Pour l'instant on ne fait rien avec tokens, on pourrait juste logger ou simuler
        System.out.println("Envoi notification PARENT à tokens : " + tokens);
    }

    public void sendNotificationToEnseignat(Long enseignantId, String title, String body) {
        String sql = "SELECT token FROM enseignant_devices WHERE enseignant_id = ?";
        List<String> tokens = jdbcTemplate.queryForList(sql, String.class, enseignantId);
        
        System.out.println("Envoi notification ENSEIGNANT à tokens : " + tokens);
    }

    public void sendNotificationToClass(Long classeId, String title, String body) {
        String sql = """
            SELECT DISTINCT pd.token 
            FROM parent_devices pd
            JOIN student e ON e.parent_id = pd.parent_id
            WHERE e.classe_id = ?
        """;
        List<String> tokens = jdbcTemplate.queryForList(sql, String.class, classeId);
        
        System.out.println("Envoi notification CLASSE à tokens : " + tokens);
    }

    public void sendNotificationToSection(Long sectionId, String title, String body) {
        String sql = """
            SELECT DISTINCT pd.token 
            FROM parent_devices pd
            JOIN student e ON e.parent_id = pd.parent_id
            WHERE e.section_id = ?
        """;
        List<String> tokens = jdbcTemplate.queryForList(sql, String.class, sectionId);
        
        System.out.println("Envoi notification SECTION à tokens : " + tokens);
    }

    // Comme on ne fait pas d'envoi réel, on supprime handleFailedTokens et deleteInvalidToken

    public void registerDevice(String userId, String token, String userType) {
        String sql;
        switch (userType) {
            case "PARENT":
                sql = "INSERT INTO parent_devices (parent_id, token) VALUES (?, ?) " +
                      "ON DUPLICATE KEY UPDATE token = ?";
                break;
            case "ENSEIGNANT":
                sql = "INSERT INTO enseignant_devices (enseignant_id, token) VALUES (?, ?) " +
                      "ON DUPLICATE KEY UPDATE token = ?";
                break;
            default:
                throw new IllegalArgumentException("Type d'utilisateur non supporté");
        }
        jdbcTemplate.update(sql, userId, token, token);
    }
}

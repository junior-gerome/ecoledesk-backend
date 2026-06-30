package com.school.platform.notification.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.school.platform.academic.application.dto.GradeDTO;
import com.school.platform.academic.application.dto.GradeResponseDTO;
import com.school.platform.notification.application.dto.NotificationDTO;
import com.school.platform.academic.domain.model.Grade;
import com.school.platform.notification.domain.model.Notification;
import com.school.platform.notification.infrastructure.persistence.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    // --- Notification Générale ---
    public void sendNotification(String destination, String message) {
        try {
            logger.debug("Envoi de notification à {} : {}", destination, message);
            messagingTemplate.convertAndSend("/topic/" + destination, message);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification : {}", e.getMessage());
        }
    }

    // --- Notification Privée ---
    public void sendPrivateNotification(String username, String message) {
        try {
            logger.debug("Envoi de notification privée à {} : {}", username, message);
            messagingTemplate.convertAndSendToUser(username, "/queue/private", message);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification privée : {}", e.getMessage());
        }
    }

    // --- Notification de nouvelle note pour un étudiant ---
    public void notifyNewGrade(Long studentId, GradeDTO gradeDTO) {
        try {
            NotificationDTO notification = new NotificationDTO(
                UUID.randomUUID().toString(),
                String.format("Nouvelle note en %s : %.2f/20", gradeDTO.getSubject(), gradeDTO.getGrade()),
                "GRADE",
                studentId,
                false,
                LocalDateTime.now()
            );

            logger.debug("Envoi de notification de note pour l'étudiant {} : {}", studentId, notification);
            messagingTemplate.convertAndSend("/topic/student/" + studentId + "/grades", notification);

            // Sauvegarder en base
            saveNotification(notification);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de note : {}", e.getMessage());
        }
    }

    public void notifyNewGrade(Long studentId, GradeResponseDTO gradeDTO) {
        try {
            NotificationDTO notification = new NotificationDTO(
                UUID.randomUUID().toString(),
                String.format("Nouvelle note en %s : %.2f/20", gradeDTO.getSubjectName(), gradeDTO.getScore()),
                "GRADE",
                studentId,
                false,
                LocalDateTime.now()
            );

            logger.debug("Envoi de notification de note pour l'etudiant {} : {}", studentId, notification);
            messagingTemplate.convertAndSend("/topic/student/" + studentId + "/grades", notification);
            saveNotification(notification);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de note : {}", e.getMessage());
        }
    }

    // --- Journalisation simple ---
    public void notifyGradeAdded(Grade grade) {
        logger.info("Nouvelle note ajoutée pour l'étudiant {} en {}",
            grade.getStudent().getLastNameStudent(),
            grade.getSubject().getNameSubject());
    }

    // --- Notification de notes en masse ---
    public void notifyBulkGradesAdded(Long classId, String subject) {
        try {
            NotificationDTO notification = new NotificationDTO(
                UUID.randomUUID().toString(),
                String.format("Nouvelles notes ajoutées en %s", subject),
                "BULK_GRADES",
                classId,
                false,
                LocalDateTime.now()
            );

            logger.debug("Envoi de notification de masse pour la classe {} : {}", classId, notification);
            messagingTemplate.convertAndSend("/topic/class/" + classId + "/grades", notification);

            // Sauvegarder en base
            saveNotification(notification);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de masse : {}", e.getMessage());
        }

        logger.info("Notes ajoutées en masse pour la classe {} en {}", classId, subject);
    }

    // --- Sauvegarde dans la base ---
    private void saveNotification(NotificationDTO notificationDTO) {
        try {
            Notification notification = new Notification();
            notification.setMessage(notificationDTO.getMessage());
            notification.setUserId(notificationDTO.getUserId());
            notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de la notification : {}", e.getMessage());
        }
    }
}

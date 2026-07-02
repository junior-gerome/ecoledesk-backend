package com.school.platform.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.notification.domain.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
} 
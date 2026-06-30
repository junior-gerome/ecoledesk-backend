package com.school.platform.notification.application.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private String message;
    private String type;
    private Long userId;
    private boolean isread;
    private LocalDateTime createdAt;

//    public NotificationDTO(String id, String message, String type, Long userId, boolean read, LocalDateTime createdAt){
//     this.id=id;
//     this.message=message;
//     this.type=type;
//     this.userId=userId;
//     this.read=read;
//     this.createdAt=createdAt;
//    } 
}
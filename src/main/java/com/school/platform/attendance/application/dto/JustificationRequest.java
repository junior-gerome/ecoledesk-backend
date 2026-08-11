package com.school.platform.attendance.application.dto;

import lombok.Data;

@Data
public class JustificationRequest {
    private Boolean justified;
    private String justificationNote;
}

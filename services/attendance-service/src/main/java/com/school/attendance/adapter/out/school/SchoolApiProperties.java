package com.school.attendance.adapter.out.school;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "school.api")
public record SchoolApiProperties(String baseUrl) {
    public SchoolApiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080/api";
        }
    }
}

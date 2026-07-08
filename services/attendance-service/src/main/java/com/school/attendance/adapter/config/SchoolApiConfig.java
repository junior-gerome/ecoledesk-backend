package com.school.attendance.adapter.config;

import com.school.attendance.adapter.out.school.SchoolApiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SchoolApiProperties.class)
public class SchoolApiConfig {
    @Bean
    RestClient schoolApiRestClient(SchoolApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}

package com.school.platform.document.infrastructure.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Construit le client MinIO a partir de la configuration applicative. */
@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        String endpoint = properties.getEndpoint();
        if (properties.isInsecure() && endpoint != null && endpoint.startsWith("https://")) {
            endpoint = "http://" + endpoint.substring("https://".length());
        }
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();
    }
}